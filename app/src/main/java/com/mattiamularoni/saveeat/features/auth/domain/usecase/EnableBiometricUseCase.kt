package com.mattiamularoni.saveeat.features.auth.domain.usecase

import com.mattiamularoni.saveeat.features.auth.domain.repository.BiometricRepository

/**
 * Abilita il login biometrico per l'utente corrente.
 *
 * Salva la preferenza localmente e avvia la sincronizzazione con il backend Supabase
 * aggiornando il campo `is_biometric_enabled` sulla tabella `users`.
 */
class EnableBiometricUseCase(
    private val biometricRepository: BiometricRepository,
) {
    suspend operator fun invoke() {
        biometricRepository.enableBiometricLogin()
    }
}
