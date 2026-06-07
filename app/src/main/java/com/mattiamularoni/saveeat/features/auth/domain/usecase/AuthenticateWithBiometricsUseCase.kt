package com.mattiamularoni.saveeat.features.auth.domain.usecase

import com.mattiamularoni.saveeat.features.auth.domain.model.BiometricAvailabilityStatus
import com.mattiamularoni.saveeat.features.auth.domain.repository.BiometricRepository

/**
 * Verifica che tutti i prerequisiti siano soddisfatti per avviare
 * il prompt di autenticazione biometrica tramite AndroidX BiometricPrompt.
 *
 * I prerequisiti richiesti sono:
 * 1. Sessione Supabase autenticata e attiva.
 * 2. Biometria abilitata dall'utente nelle preferenze.
 * 3. Dispositivo con biometria disponibile e credenziali registrate.
 *
 * @return `true` se il prompt biometrico può essere mostrato, `false` altrimenti.
 */
class AuthenticateWithBiometricsUseCase(
    private val biometricRepository: BiometricRepository
) {
    operator fun invoke(): Boolean =
        biometricRepository.isSessionAuthenticated() &&
        biometricRepository.isBiometricLoginEnabled() &&
        biometricRepository.getBiometricAvailabilityStatus() == BiometricAvailabilityStatus.Available
}
