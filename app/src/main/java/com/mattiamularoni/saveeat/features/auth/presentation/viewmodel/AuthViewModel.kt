package com.mattiamularoni.saveeat.features.auth.presentation.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mattiamularoni.saveeat.BuildConfig
import com.mattiamularoni.saveeat.features.auth.domain.model.BiometricAvailabilityStatus
import com.mattiamularoni.saveeat.features.auth.domain.repository.BiometricRepository
import com.mattiamularoni.saveeat.features.auth.domain.usecase.CheckBiometricAvailabilityUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.DisableBiometricUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.EnableBiometricUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.ObserveSessionStatusUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.RestoreAuthenticatedSessionUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignInWithEmailUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignInWithGoogleUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignOutUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignUpWithEmailUseCase
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State per le schermate di autenticazione email/password.
 *
 * - [Idle]: nessuna operazione in corso
 * - [Loading]: operazione in corso
 * - [Success]: operazione completata con successo
 * - [Error]: operazione fallita con messaggio d'errore
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String = "") : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

/**
 * UI State per il flusso di autenticazione biometrica.
 *
 * - [Idle]: schermata biometrica non attiva
 * - [Authenticating]: prompt biometrico mostrato all'utente
 * - [Authenticated]: autenticazione biometrica completata con successo
 * - [Error]: autenticazione fallita con descrizione dell'errore
 */
sealed class BiometricUiState {
    object Idle : BiometricUiState()
    object Authenticating : BiometricUiState()
    object Authenticated : BiometricUiState()
    data class Error(val message: String) : BiometricUiState()
}

/**
 * Effetti one-time per le schermate di autenticazione email/password.
 */
sealed class AuthEffect {
    data class ShowSnackbar(val message: String) : AuthEffect()
}

/**
 * Effetti one-time relativi al flusso biometrico.
 *
 * Separati da [AuthEffect] per non interferire con la gestione esistente
 * in [AuthScreen], che non deve essere modificata.
 */
sealed class BiometricEffect {
    /**
     * Emessa dopo un login email/password riuscito quando la biometria
     * è disponibile sul dispositivo ma non ancora abilitata dall'utente.
     * Usata da [AuthNavigation] per mostrare il dialog di proposta.
     */
    object ProposeEnablement : BiometricEffect()
}

/**
 * ViewModel per le schermate di autenticazione.
 *
 * Gestisce login/registrazione email, logout, flusso biometrico e UI state.
 *
 * ### biometricRequired e multi-istanza
 * Il NavHost usa il ViewModel Activity-scoped per osservare [biometricRequired].
 * Le schermate all'interno del NavHost usano istanze NavBackStackEntry-scoped.
 * Per garantire coerenza, il flag di sessione confermata è mantenuto nel
 * [BiometricRepository] (Koin `single`), condiviso tra tutte le istanze.
 *
 * ### Flusso di navigazione
 * - [biometricRequired] = `null`: sessione non ancora risolta, NavHost in attesa.
 * - [biometricRequired] = `true`: mostrare [BiometricRoute] da [LoginRoute].
 * - [biometricRequired] = `false`: navigare a [HomeRoute] da [LoginRoute].
 * - Navigazione da [BiometricRoute] → Home: gestita dai callback del composable.
 * - Navigazione da [BiometricRoute] → Login: tramite sign-out + nav guard automatico.
 */
class AuthViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val enableBiometricUseCase: EnableBiometricUseCase,
    private val disableBiometricUseCase: DisableBiometricUseCase,
    private val checkBiometricAvailabilityUseCase: CheckBiometricAvailabilityUseCase,
    private val restoreAuthenticatedSessionUseCase: RestoreAuthenticatedSessionUseCase,
    private val biometricRepository: BiometricRepository,
    observeSessionStatusUseCase: ObserveSessionStatusUseCase
) : ViewModel() {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _authEffect = MutableSharedFlow<AuthEffect>()
    val authEffect: SharedFlow<AuthEffect> = _authEffect.asSharedFlow()

    private val _biometricUiState = MutableStateFlow<BiometricUiState>(BiometricUiState.Idle)
    val biometricUiState: StateFlow<BiometricUiState> = _biometricUiState.asStateFlow()

    private val _biometricEffect = MutableSharedFlow<BiometricEffect>()
    val biometricEffect: SharedFlow<BiometricEffect> = _biometricEffect.asSharedFlow()

    /**
     * Indica se la schermata di sblocco biometrico deve essere mostrata.
     *
     * `null` = sessione non ancora risolta (Supabase in caricamento).
     * `true` = sessione attiva + biometria abilitata + dispositivo supportato + non confermato.
     * `false` = biometria non richiesta o già confermata per questa sessione app.
     *
     * Il NavHost usa `null` come guardia per evitare navigazioni premature.
     * Il NavHost usa solo il valore Activity-scoped di questo ViewModel.
     */
    private val _biometricRequired = MutableStateFlow<Boolean?>(null)
    val biometricRequired: StateFlow<Boolean?> = _biometricRequired.asStateFlow()

    val sessionStatus: StateFlow<SessionStatus> = observeSessionStatusUseCase()

    init {
        viewModelScope.launch {
            sessionStatus.collect { status ->
                _biometricRequired.value = when (status) {
                    is SessionStatus.Authenticated -> restoreAuthenticatedSessionUseCase()
                    else -> false
                }
            }
        }
    }

    /**
     * Effettua il login con email e password tramite Supabase Auth.
     *
     * Al completamento con successo marca la sessione come confermata (via [BiometricRepository]
     * condiviso) e verifica se proporre l'attivazione biometrica all'utente.
     *
     * @param email l'indirizzo email dell'utente.
     * @param password la password dell'utente.
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authUiState.value = AuthUiState.Loading
                signInWithEmailUseCase(email, password)
                // Il login esplicito con password conta come conferma identità per questa sessione
                biometricRepository.confirmSession()
                _authUiState.value = AuthUiState.Success("Signed in successfully")

                val availability = checkBiometricAvailabilityUseCase()
                if (availability == BiometricAvailabilityStatus.Available &&
                    !biometricRepository.isBiometricLoginEnabled()
                ) {
                    _biometricEffect.emit(BiometricEffect.ProposeEnablement)
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Sign-in failed. Please try again."
                _authUiState.value = AuthUiState.Error(errorMessage)
                _authEffect.emit(AuthEffect.ShowSnackbar(errorMessage))
            }
        }
    }

    /**
     * Avvia il flusso Google Sign-In tramite Credential Manager API.
     *
     * Mostra il picker account Google, estrae l'ID Token e lo usa per autenticarsi
     * su Supabase. Al successo, esegue l'upsert del profilo nella tabella `users`
     * e, se la biometria è disponibile ma non abilitata, propone di attivarla.
     *
     * @param context Activity context richiesto da [CredentialManager] per il picker.
     */
    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    signInWithGoogleUseCase(idToken)
                    biometricRepository.confirmSession()
                    _authUiState.value = AuthUiState.Success()

                    val availability = checkBiometricAvailabilityUseCase()
                    if (availability == BiometricAvailabilityStatus.Available &&
                        !biometricRepository.isBiometricLoginEnabled()
                    ) {
                        _biometricEffect.emit(BiometricEffect.ProposeEnablement)
                    }
                } else {
                    val msg = "Credenziale non supportata"
                    _authUiState.value = AuthUiState.Error(msg)
                    _authEffect.emit(AuthEffect.ShowSnackbar(msg))
                }
            } catch (e: GetCredentialCancellationException) {
                _authUiState.value = AuthUiState.Idle
            } catch (e: GetCredentialException) {
                val msg = "Accesso con Google non riuscito. Riprova."
                _authUiState.value = AuthUiState.Error(msg)
                _authEffect.emit(AuthEffect.ShowSnackbar(msg))
            } catch (e: Exception) {
                val msg = e.message ?: "Errore imprevisto. Riprova."
                _authUiState.value = AuthUiState.Error(msg)
                _authEffect.emit(AuthEffect.ShowSnackbar(msg))
            }
        }
    }

    /**
     * Registra un nuovo account con email e password tramite Supabase Auth.
     *
     * @param email l'indirizzo email dell'utente.
     * @param password la password desiderata.
     * @param firstName il nome dell'utente.
     * @param lastName il cognome dell'utente.
     */
    fun signUp(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            try {
                _authUiState.value = AuthUiState.Loading
                signUpWithEmailUseCase(email, password, firstName, lastName)
                _authUiState.value = AuthUiState.Success("Registered successfully. Please check your email to confirm.")
                _authEffect.emit(AuthEffect.ShowSnackbar("Check your email for confirmation"))
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Registration failed. Please try again."
                _authUiState.value = AuthUiState.Error(errorMessage)
                _authEffect.emit(AuthEffect.ShowSnackbar(errorMessage))
            }
        }
    }

    /**
     * Effettua il logout dell'utente corrente.
     *
     * Resetta la conferma di sessione biometrica in modo che al prossimo accesso
     * venga richiesta la verifica dell'identità.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                signOutUseCase()
                biometricRepository.resetSessionConfirmation()
                _authUiState.value = AuthUiState.Idle
                _authEffect.emit(AuthEffect.ShowSnackbar("Signed out successfully"))
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Sign-out failed. Please try again."
                _authUiState.value = AuthUiState.Error(errorMessage)
                _authEffect.emit(AuthEffect.ShowSnackbar(errorMessage))
            }
        }
    }

    /**
     * Chiamata dalla UI quando il prompt AndroidX [BiometricPrompt] ha avuto successo.
     *
     * Marca la sessione come confermata tramite [BiometricRepository] (Koin `single`),
     * garantendo che anche l'istanza Activity-scoped del ViewModel veda la conferma
     * al prossimo refresh del token Supabase.
     */
    fun onBiometricSuccess() {
        biometricRepository.confirmSession()
        _biometricUiState.value = BiometricUiState.Authenticated
        _biometricRequired.value = false
    }

    /**
     * Chiamata dalla UI quando il prompt biometrico restituisce un errore.
     *
     * Non invalida la sessione Supabase: l'utente può riprovare o usare la password.
     *
     * @param errorCode codice errore di [BiometricPrompt].
     * @param errString messaggio leggibile dall'utente restituito dal sistema.
     */
    fun onBiometricError(errorCode: Int, errString: CharSequence) {
        _biometricUiState.value = BiometricUiState.Error(errString.toString())
    }

    /**
     * Chiamata quando l'utente sceglie di usare la password invece della biometria.
     *
     * Avvia il sign-out da Supabase in background: quando la sessione diventa
     * [SessionStatus.NotAuthenticated], il nav guard in [SaveEatNavHost] reindirizza
     * automaticamente a [LoginRoute] senza necessità di callback esplicita.
     * Non imposta [confirmSession] qui: verrà impostato da [signIn] al prossimo login.
     */
    fun onBiometricFallbackToPassword() {
        _biometricUiState.value = BiometricUiState.Idle
        viewModelScope.launch {
            try {
                signOutUseCase()
                // Nessun resetSessionConfirmation: l'utente non è ancora autenticato
            } catch (_: Exception) { /* il nav guard gestirà NotAuthenticated se necessario */ }
        }
    }

    /**
     * Abilita il login biometrico per l'utente corrente.
     * Salva la preferenza localmente e sincronizza con Supabase in background.
     */
    fun enableBiometricLogin() {
        viewModelScope.launch {
            try {
                enableBiometricUseCase()
                _authEffect.emit(AuthEffect.ShowSnackbar("Accesso biometrico abilitato"))
            } catch (e: Exception) {
                _authEffect.emit(AuthEffect.ShowSnackbar("Errore nell'abilitazione biometrica"))
            }
        }
    }

    /**
     * Disabilita il login biometrico per l'utente corrente.
     * Rimuove la preferenza localmente e sincronizza con Supabase in background.
     */
    fun disableBiometricLogin() {
        viewModelScope.launch {
            try {
                disableBiometricUseCase()
                _authEffect.emit(AuthEffect.ShowSnackbar("Accesso biometrico disabilitato"))
            } catch (e: Exception) {
                _authEffect.emit(AuthEffect.ShowSnackbar("Errore nella disabilitazione biometrica"))
            }
        }
    }

    /** Resetta lo stato UI a Idle. Chiamare quando si transita tra schermate o si puliscono errori. */
    fun resetState() {
        _authUiState.value = AuthUiState.Idle
    }
}
