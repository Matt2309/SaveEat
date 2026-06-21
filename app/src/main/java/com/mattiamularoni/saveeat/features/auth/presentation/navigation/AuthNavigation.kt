package com.mattiamularoni.saveeat.features.auth.presentation.navigation

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt as BiometricCompat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.BiometricRoute
import com.mattiamularoni.saveeat.core.navigation.LoginRoute
import com.mattiamularoni.saveeat.features.auth.presentation.ui.AuthScreen
import com.mattiamularoni.saveeat.features.auth.presentation.ui.BiometricUnlockScreen
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthViewModel
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.BiometricUiState

/**
 * Composable di navigazione per la schermata di autenticazione email/password.
 *
 * Oltre a mostrare [AuthScreen], osserva lo stato di proposta biometrica per
 * esporre un [AlertDialog] che propone l'attivazione del login biometrico subito dopo il primo accesso.
 *
 * Il dialog è gestito qui (non in [AuthScreen]) per evitare modifiche alla UI esistente
 * e rispettare la separazione tra navigazione e schermata.
 */
fun NavGraphBuilder.authScreen(
    authViewModel: AuthViewModel,
    onNavigateToPantry: () -> Unit = {}
) {
    composable<LoginRoute> {
        // Use the Activity-scoped instance from the NavHost so that showBiometricProposal
        // and sign-in logic are observed on the same ViewModel instance.
        val showBiometricDialog by authViewModel.showBiometricProposal.collectAsState()

        if (showBiometricDialog) {
            AlertDialog(
                onDismissRequest = { authViewModel.onBiometricProposalDismissed() },
                title = { Text("Abilita accesso biometrico") },
                text = { Text("Vuoi accedere più velocemente con impronta digitale o Face ID?") },
                confirmButton = {
                    TextButton(onClick = {
                        authViewModel.enableBiometricLogin()
                        authViewModel.onBiometricProposalDismissed()
                    }) {
                        Text("Abilita")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        authViewModel.onBiometricProposalDismissed()
                    }) {
                        Text("Non ora")
                    }
                }
            )
        }

        AuthScreen(viewModel = authViewModel, onNavigateToPantry = onNavigateToPantry)
    }
}

/**
 * Composable di navigazione per la schermata di sblocco biometrico.
 *
 * Avvia automaticamente il prompt biometrico al primo render tramite
 * [android.hardware.biometrics.BiometricPrompt] (API 28+), compatibile con
 * [androidx.activity.ComponentActivity] senza richiedere [androidx.fragment.app.FragmentActivity].
 *
 * ### Navigazione al successo
 * Quando [BiometricUiState.Authenticated] viene emesso, [onNavigateToHome] viene chiamato.
 *
 * ### Fallback a password
 * Quando l'utente sceglie "Usa password", [AuthViewModel.onBiometricFallbackToPassword]
 * avvia il sign-out da Supabase. Al completamento, [SessionStatus.NotAuthenticated]
 * viene emesso e il nav guard in SaveEatNavHost reindirizza automaticamente a LoginRoute.
 *
 * @param onNavigateToHome chiamata dopo autenticazione biometrica riuscita.
 */
fun NavGraphBuilder.biometricScreen(
    authViewModel: AuthViewModel,
    onNavigateToHome: () -> Unit
) {
    composable<BiometricRoute> {
        val context = LocalContext.current
        val biometricUiState by authViewModel.biometricUiState.collectAsState()

        // Auto-avvia il prompt biometrico solo se non già autenticato in questa sessione:
        // evita ri-prompt se la composable rientra in composizione mentre biometricUiState
        // è già Authenticated (difesa in profondità, oltre al fix della causa radice in
        // SaveEatNavHost che usa ProcessLifecycleOwner per evitare il re-trigger da rotazione).
        LaunchedEffect(biometricUiState) {
            if (biometricUiState !is BiometricUiState.Authenticated) {
                launchBiometricPrompt(context, authViewModel)
            }
        }

        // Naviga a Home dopo autenticazione biometrica riuscita
        LaunchedEffect(biometricUiState) {
            if (biometricUiState is BiometricUiState.Authenticated) {
                onNavigateToHome()
            }
        }

        BiometricUnlockScreen(
            subtitle = "Bentornato su SaveEat",
            onTapFingerprint = { launchBiometricPrompt(context, authViewModel) },
            onUsePassword = { authViewModel.onBiometricFallbackToPassword() },
            errorMessage = (biometricUiState as? BiometricUiState.Error)?.message
        )
    }
}

/**
 * Avvia il prompt di autenticazione biometrica.
 *
 * Usa [android.hardware.biometrics.BiometricPrompt] (API nativa) invece di
 * [androidx.biometric.BiometricPrompt] per evitare il requisito di [androidx.fragment.app.FragmentActivity].
 * Su API < 28, delega l'errore al ViewModel — in pratica irraggiungibile perché
 * [RestoreAuthenticatedSessionUseCase] restituisce `false` su dispositivi senza biometria.
 */
private fun launchBiometricPrompt(context: Context, viewModel: AuthViewModel) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        viewModel.onBiometricError(-1, "Biometria non supportata su questa versione di Android.")
        return
    }
    launchBiometricPromptApi28(context, viewModel)
}

@RequiresApi(Build.VERSION_CODES.P)
private fun launchBiometricPromptApi28(context: Context, viewModel: AuthViewModel) {
    val executor = ContextCompat.getMainExecutor(context)
    val cancellationSignal = CancellationSignal()

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            viewModel.onBiometricSuccess()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            // Stessi valori interi nell'API nativa (13, 10) — usiamo le costanti AndroidX
            val silentErrors = setOf(
                BiometricCompat.ERROR_NEGATIVE_BUTTON,
                BiometricCompat.ERROR_USER_CANCELED
            )
            if (errorCode !in silentErrors) {
                viewModel.onBiometricError(errorCode, errString)
            }
        }

        override fun onAuthenticationFailed() {
            viewModel.onBiometricError(-1, "Autenticazione non riconosciuta. Riprova.")
        }
    }

    val prompt = BiometricPrompt.Builder(context)
        .setTitle("Sblocca SaveEat")
        .setSubtitle("Usa la biometria per accedere")
        .setNegativeButton(
            "Annulla",
            executor
        ) { _, _ -> /* silenzioso: l'utente usa il pulsante "Usa password" nella schermata */ }
        .build()

    prompt.authenticate(cancellationSignal, executor, callback)
}