package com.mattiamularoni.saveeat.features.auth.domain.usecase

import com.mattiamularoni.saveeat.features.auth.domain.repository.AuthRepository

/**
 * Use case for signing up with email and password.
 *
 * Delegates to AuthRepository.signUpWithEmail() for Supabase registration.
 * Allows domain layer to maintain clean separation from data layer.
 */
class SignUpWithEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, firstName: String, lastName: String) {
        authRepository.signUpWithEmail(email, password, firstName, lastName)
    }
}
