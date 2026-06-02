package com.mattiamularoni.saveeat.features.leaderboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.features.leaderboard.presentation.domain.GetLeaderboardUseCase
import com.mattiamularoni.saveeat.features.leaderboard.presentation.state.LeaderboardUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Sealed interface per gli effetti secondari del ViewModel.
 */
sealed interface LeaderboardEffect {
    data class ShowSnackbar(val message: String) : LeaderboardEffect
}

/**
 * ViewModel per la gestione della leaderboard.
 *
 * Responsabilità:
 * - Osservare dati leaderboard dal use case
 * - Gestire UI state (Loading, Success, Error)
 * - Supportare azioni (refresh manuale)
 * - Emettere effetti (snackbar, etc.)
 */
class LeaderboardViewModel(
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Loading)
    private val _effects = MutableSharedFlow<LeaderboardEffect>()

    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()
    val effects: SharedFlow<LeaderboardEffect> = _effects.asSharedFlow()

    private var hasLoaded = false
    private var errorMessage: String? = null

    init {
        observeLeaderboard()
    }

    /**
     * Richiede un refresh manuale della leaderboard.
     *
     * Resetta lo stato a Loading e osserva nuovi dati.
     */
    fun onRefresh() {
        hasLoaded = false
        observeLeaderboard()
    }

    private fun observeLeaderboard() {
        viewModelScope.launch {
            getLeaderboardUseCase()
                .onEach { users ->
                    hasLoaded = true
                    errorMessage = null
                    _uiState.value = LeaderboardUiState.Success(users)
                }
                .catch { throwable ->
                    hasLoaded = true
                    errorMessage = throwable.message ?: "Unable to load leaderboard"
                    _uiState.value = LeaderboardUiState.Error(errorMessage.orEmpty())
                    _effects.emit(LeaderboardEffect.ShowSnackbar(errorMessage.orEmpty()))
                }
                .collect()
        }
    }
}
