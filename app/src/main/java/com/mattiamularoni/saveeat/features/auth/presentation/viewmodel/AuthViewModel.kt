package com.mattiamularoni.saveeat.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.features.auth.domain.usecase.ObserveSessionStatusUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignInWithEmailUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignOutUseCase
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignUpWithEmailUseCase
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for authentication screens.
 *
 * Represents the current state of authentication operations:
 * - [Idle]: No operation in progress
 * - [Loading]: Sign-in/sign-up operation in progress
 * - [Success]: Operation completed successfully (optional message)
 * - [Error]: Operation failed (error message provided)
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String = "") : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

/**
 * One-time effects for authentication screens.
 *
 * Events that should be consumed once (e.g., snackbar notifications).
 * Unlike state, effects are not stored and should be cleared after consumption.
 */
sealed class AuthEffect {
    data class ShowSnackbar(val message: String) : AuthEffect()
}

/**
 * ViewModel for authentication screens.
 *
 * Manages:
 * - Email/password sign-in and sign-up flows
 * - Sign-out functionality
 * - Session status observation for navigation
 * - UI state management (loading, success, error)
 * - One-time effects (snackbars)
 * - Input validation
 *
 * All operations are performed on viewModelScope to respect lifecycle.
 * Exceptions are caught and converted to user-friendly error messages.
 */
class AuthViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signOutUseCase: SignOutUseCase,
    observeSessionStatusUseCase: ObserveSessionStatusUseCase
) : ViewModel() {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _authEffect = MutableSharedFlow<AuthEffect>()
    val authEffect: SharedFlow<AuthEffect> = _authEffect.asSharedFlow()

    val sessionStatus: StateFlow<SessionStatus> = observeSessionStatusUseCase()

    /**
     * Sign in with email and password.
     *
     * Performs validation before attempting sign-in.
     * Updates UI state to Loading, then Success or Error based on result.
     * Emits snackbar effect on failure.
     *
     * @param email The user's email address
     * @param password The user's password
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authUiState.value = AuthUiState.Loading
                signInWithEmailUseCase(email, password)
                _authUiState.value = AuthUiState.Success("Signed in successfully")
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Sign-in failed. Please try again."
                _authUiState.value = AuthUiState.Error(errorMessage)
                _authEffect.emit(AuthEffect.ShowSnackbar(errorMessage))
            }
        }
    }

    /**
     * Sign up with email and password.
     *
     * Performs validation before attempting sign-up.
     * Updates UI state to Loading, then Success or Error based on result.
     * Emits snackbar effect on failure or success with instructions.
     *
     * @param email The user's email address
     * @param password The user's desired password
     */
    fun signUp(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            try {
                _authUiState.value = AuthUiState.Loading
                signUpWithEmailUseCase(email, password, firstName, lastName)
                _authUiState.value = AuthUiState.Success("Registered successfully. Please check your email to confirm.")
                _authEffect.emit(AuthEffect.ShowSnackbar("Check your email for confirmation"))
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Registration failed. Please try again."
                _authUiState.value = AuthUiState.Error(errorMessage)
                _authEffect.emit(AuthEffect.ShowSnackbar(errorMessage))
            }
        }
    }

    /**
     * Sign out the current user.
     *
     * Clears the session and resets UI state to Idle.
     * Emits snackbar effect on failure.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                signOutUseCase()
                _authUiState.value = AuthUiState.Idle
                _authEffect.emit(AuthEffect.ShowSnackbar("Signed out successfully"))
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Sign-out failed. Please try again."
                _authUiState.value = AuthUiState.Error(errorMessage)
                _authEffect.emit(AuthEffect.ShowSnackbar(errorMessage))
            }
        }
    }

    /**
     * Reset UI state to Idle.
     *
     * Call this when transitioning between screens or to clear error states.
     */
    fun resetState() {
        _authUiState.value = AuthUiState.Idle
    }
}
