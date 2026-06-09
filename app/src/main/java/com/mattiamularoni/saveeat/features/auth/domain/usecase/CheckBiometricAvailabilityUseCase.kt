package com.mattiamularoni.saveeat.features.auth.domain.usecase

import com.mattiamularoni.saveeat.features.auth.domain.model.BiometricAvailabilityStatus
import com.mattiamularoni.saveeat.features.auth.domain.repository.BiometricRepository

/**
 * Verifica se il dispositivo supporta l'autenticazione biometrica
 * e se l'utente ha registrato almeno una credenziale valida.
 *
 * @return lo stato di disponibilità biometrica sul dispositivo.
 */
class CheckBiometricAvailabilityUseCase(
    private val biometricRepository: BiometricRepository
) {
    operator fun invoke(): BiometricAvailabilityStatus =
        biometricRepository.getBiometricAvailabilityStatus()
}
