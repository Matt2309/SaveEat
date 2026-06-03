package com.mattiamularoni.saveeat.features.auth.domain.usecase

import com.mattiamularoni.saveeat.features.auth.domain.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Use case for observing session status changes.
 *
 * Exposes the AuthRepository.sessionStatus StateFlow for reactive UI observation.
 * Allows UI layer to automatically respond to authentication state changes.
 *
 * Usage:
 * ```
 * val sessionStatus = observeSessionStatusUseCase().collect { status ->
 *     // React to session status changes (login, logout, etc.)
 * }
 * ```
 */
class ObserveSessionStatusUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): StateFlow<SessionStatus> {
        return authRepository.sessionStatus
    }
}
