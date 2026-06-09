package com.mattiamularoni.saveeat.features.auth.data.local

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import com.mattiamularoni.saveeat.features.auth.domain.model.BiometricAvailabilityStatus
import com.mattiamularoni.saveeat.features.auth.domain.repository.BiometricRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementazione di [BiometricRepository].
 *
 * Combina tre sorgenti:
 * - [BiometricManager] di AndroidX per verificare le capacità hardware del dispositivo.
 * - [BiometricPreferenceDataSource] per la preferenza locale persistente e il flag di sessione.
 * - [SupabaseClient] per la sincronizzazione backend (tabella `users`, colonna `is_biometric_enabled`).
 *
 * Questa classe è registrata come `single` in Koin: il flag [BiometricPreferenceDataSource.biometricConfirmedThisSession]
 * è quindi condiviso tra tutte le istanze di ViewModel che iniettano [BiometricRepository].
 */
class BiometricRepositoryImpl(
    context: Context,
    private val supabaseClient: SupabaseClient,
    private val preferenceDataSource: BiometricPreferenceDataSource
) : BiometricRepository {

    private val biometricManager = BiometricManager.from(context)

    /**
     * Verifica se il dispositivo supporta la biometria e se l'utente
     * ha registrato almeno una credenziale valida (impronta, Face ID o PIN/pattern).
     *
     * @return lo stato di disponibilità hardware della biometria.
     */
    override fun getBiometricAvailabilityStatus(): BiometricAvailabilityStatus =
        when (biometricManager.canAuthenticate(BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailabilityStatus.Available
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailabilityStatus.NotEnrolled
            else -> BiometricAvailabilityStatus.NotAvailable
        }

    /**
     * Legge la preferenza locale: l'utente ha scelto di usare la biometria su questo dispositivo.
     *
     * @return `true` se la biometria è abilitata dall'utente, `false` altrimenti.
     */
    override fun isBiometricLoginEnabled(): Boolean =
        preferenceDataSource.isBiometricEnabled()

    /**
     * Verifica se esiste una sessione Supabase autenticata e attiva.
     *
     * @return `true` se [SessionStatus] è [SessionStatus.Authenticated].
     */
    override fun isSessionAuthenticated(): Boolean =
        supabaseClient.auth.sessionStatus.value is SessionStatus.Authenticated

    /**
     * Verifica se l'identità dell'utente è già stata confermata nella sessione app corrente.
     *
     * Il valore viene letto dal [BiometricPreferenceDataSource] condiviso (Koin `single`),
     * garantendo coerenza tra le istanze Activity-scoped e NavBackStackEntry-scoped del ViewModel.
     *
     * @return `true` se la sessione è già stata confermata.
     */
    override fun isBiometricConfirmedThisSession(): Boolean =
        preferenceDataSource.biometricConfirmedThisSession

    /**
     * Segna la sessione corrente come confermata.
     * Chiamare dopo biometria riuscita o login esplicito con password.
     */
    override fun confirmSession() {
        preferenceDataSource.confirmSession()
    }

    /**
     * Resetta la conferma di sessione.
     * Chiamare al logout per richiedere verifica al prossimo accesso.
     */
    override fun resetSessionConfirmation() {
        preferenceDataSource.resetSessionConfirmation()
    }

    /**
     * Abilita il login biometrico: salva la preferenza localmente e sincronizza con Supabase.
     */
    override suspend fun enableBiometricLogin() {
        preferenceDataSource.setBiometricEnabled(true)
        syncBiometricPreferenceToBackend(true)
    }

    /**
     * Disabilita il login biometrico: salva la preferenza localmente e sincronizza con Supabase.
     */
    override suspend fun disableBiometricLogin() {
        preferenceDataSource.setBiometricEnabled(false)
        syncBiometricPreferenceToBackend(false)
    }

    /**
     * Aggiorna il campo `is_biometric_enabled` sulla tabella `users` di Supabase.
     *
     * Operazione best-effort: il fallimento del network non invalida la preferenza locale
     * già salvata. L'errore viene silenziosamente ignorato con [runCatching].
     * La query viene eseguita su [Dispatchers.IO] per non bloccare il Main Thread.
     *
     * @param enabled il nuovo valore da sincronizzare.
     */
    override suspend fun syncBiometricPreferenceToBackend(enabled: Boolean) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        runCatching {
            withContext(Dispatchers.IO) {
                supabaseClient
                    .from("users")
                    .update(mapOf<String, Any>("is_biometric_enabled" to enabled)) {
                        filter {
                            eq("id", userId)
                        }
                    }
            }
        }
    }
}
