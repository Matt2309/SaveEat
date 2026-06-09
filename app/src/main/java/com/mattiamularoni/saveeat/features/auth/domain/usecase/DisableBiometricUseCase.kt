package com.mattiamularoni.saveeat.features.auth.domain.usecase

import com.mattiamularoni.saveeat.features.auth.domain.repository.BiometricRepository

/**
 * Disabilita il login biometrico per l'utente corrente.
 *
 * Rimuove la preferenza localmente e aggiorna il campo `is_biometric_enabled`
 * sulla tabella `users` di Supabase a `false`.
 */
class DisableBiometricUseCase(
    private val biometricRepository: BiometricRepository
) {
    suspend operator fun invoke() {
        biometricRepository.disableBiometricLogin()
    }
}
