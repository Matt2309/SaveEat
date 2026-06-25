package com.mattiamularoni.saveeat.features.auth.domain.usecase

import com.mattiamularoni.saveeat.features.auth.domain.repository.AuthRepository

/**
 * Use case for signing in with email and password.
 *
 * Delegates to AuthRepository.signInWithEmail() for Supabase authentication.
 * Allows domain layer to maintain clean separation from data layer.
 */
class SignInWithEmailUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ) {
        authRepository.signInWithEmail(email, password)
    }
}
