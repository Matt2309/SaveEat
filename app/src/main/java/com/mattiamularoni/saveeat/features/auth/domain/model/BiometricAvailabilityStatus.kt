package com.mattiamularoni.saveeat.features.auth.domain.model

/**
 * Rappresenta lo stato di disponibilità della biometria sul dispositivo corrente.
 *
 * Usato da [CheckBiometricAvailabilityUseCase] e da [RestoreAuthenticatedSessionUseCase]
 * per determinare se il prompt biometrico può essere mostrato.
 */
sealed class BiometricAvailabilityStatus {
    /** Il dispositivo supporta la biometria e l'utente ha credenziali registrate. */
    object Available : BiometricAvailabilityStatus()

    /**
     * Il dispositivo supporta la biometria ma l'utente non ha ancora
     * registrato impronta, Face ID o PIN/pattern come fallback.
     */
    object NotEnrolled : BiometricAvailabilityStatus()

    /** Il dispositivo non supporta la biometria (hardware assente o disabilitato da policy). */
    object NotAvailable : BiometricAvailabilityStatus()
}
