package com.mattiamularoni.saveeat.features.auth.domain.usecase

import com.mattiamularoni.saveeat.features.auth.domain.repository.AuthRepository

/**
 * Use case for signing out the current user.
 *
 * Delegates to AuthRepository.signOut() to clear Supabase session.
 * Allows domain layer to maintain clean separation from data layer.
 */
class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.signOut()
    }
}
