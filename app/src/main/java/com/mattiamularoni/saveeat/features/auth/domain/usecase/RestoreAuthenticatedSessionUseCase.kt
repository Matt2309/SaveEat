package com.mattiamularoni.saveeat.features.auth.domain.usecase

import com.mattiamularoni.saveeat.features.auth.domain.model.BiometricAvailabilityStatus
import com.mattiamularoni.saveeat.features.auth.domain.repository.BiometricRepository

/**
 * Determina se, al ripristino della sessione autenticata, è necessario
 * mostrare la schermata di sblocco biometrico.
 *
 * Viene invocata ogni volta che [SessionStatus.Authenticated] viene emesso
 * (inclusi i refresh automatici del token). Grazie al flag
 * [BiometricRepository.isBiometricConfirmedThisSession], la schermata viene
 * richiesta SOLO alla prima autenticazione della sessione app, non ai successivi
 * refresh del token che non richiedono nuova verifica dell'identità.
 *
 * Condizioni per cui la schermata biometrica viene richiesta (tutte devono essere vere):
 * 1. L'identità NON è ancora stata confermata nella sessione app corrente.
 * 2. Sessione Supabase autenticata e attiva.
 * 3. L'utente ha abilitato il login biometrico nelle preferenze locali.
 * 4. Il dispositivo ha biometria disponibile e credenziali registrate.
 *
 * @return `true` se la schermata biometrica deve essere mostrata.
 */
class RestoreAuthenticatedSessionUseCase(
    private val biometricRepository: BiometricRepository,
) {
    operator fun invoke(): Boolean =
        !biometricRepository.isBiometricConfirmedThisSession() &&
            biometricRepository.isSessionAuthenticated() &&
            biometricRepository.isBiometricLoginEnabled() &&
            biometricRepository.getBiometricAvailabilityStatus() == BiometricAvailabilityStatus.Available
}
