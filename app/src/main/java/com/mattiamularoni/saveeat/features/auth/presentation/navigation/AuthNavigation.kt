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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.BiometricRoute
import com.mattiamularoni.saveeat.core.navigation.LoginRoute
import com.mattiamularoni.saveeat.features.auth.presentation.ui.AuthScreen
import com.mattiamularoni.saveeat.features.auth.presentation.ui.BiometricUnlockScreen
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthViewModel
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.BiometricEffect
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.BiometricUiState
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

/**
 * Composable di navigazione per la schermata di autenticazione email/password.
 *
 * Oltre a mostrare [AuthScreen], osserva [AuthViewModel.biometricEffect] per
 * intercettare [BiometricEffect.ProposeEnablement] ed esporre un [AlertDialog]
 * che propone l'attivazione del login biometrico subito dopo il primo accesso.
 *
 * Il dialog è gestito qui (non in [AuthScreen]) per evitare modifiche alla UI esistente
 * e rispettare la separazione tra navigazione e schermata.
 */
fun NavGraphBuilder.authScreen(
    onNavigateToPantry: () -> Unit = {}
) {
    composable<LoginRoute> {
        val viewModel: AuthViewModel = koinViewModel()
        var showBiometricDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.biometricEffect.collectLatest { effect ->
                when (effect) {
                    is BiometricEffect.ProposeEnablement -> showBiometricDialog = true
                }
            }
        }

        if (showBiometricDialog) {
            AlertDialog(
                onDismissRequest = { showBiometricDialog = false },
                title = { Text("Abilita accesso biometrico") },
                text = { Text("Vuoi accedere più velocemente con impronta digitale o Face ID?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.enableBiometricLogin()
                        showBiometricDialog = false
                    }) {
                        Text("Abilita")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBiometricDialog = false }) {
                        Text("Non ora")
                    }
                }
            )
        }

        AuthScreen(onNavigateToPantry = onNavigateToPantry)
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
    onNavigateToHome: () -> Unit
) {
    composable<BiometricRoute> {
        val viewModel: AuthViewModel = koinViewModel()
        val context = LocalContext.current
        val biometricUiState by viewModel.biometricUiState.collectAsState()

        // Auto-avvia il prompt biometrico appena la schermata appare
        LaunchedEffect(Unit) {
            launchBiometricPrompt(context, viewModel)
        }

        // Naviga a Home dopo autenticazione biometrica riuscita
        LaunchedEffect(biometricUiState) {
            if (biometricUiState is BiometricUiState.Authenticated) {
                onNavigateToHome()
            }
        }

        BiometricUnlockScreen(
            subtitle = "Bentornato su SaveEat",
            onTapFingerprint = { launchBiometricPrompt(context, viewModel) },
            onUsePassword = { viewModel.onBiometricFallbackToPassword() },
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
