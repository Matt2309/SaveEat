package com.mattiamularoni.saveeat.features.auth.domain.usecase

import com.mattiamularoni.saveeat.features.auth.domain.repository.AuthRepository

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(idToken: String) {
        authRepository.signInWithGoogle(idToken)
    }
}
