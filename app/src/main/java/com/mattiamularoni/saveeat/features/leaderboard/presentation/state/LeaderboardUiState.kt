package com.mattiamularoni.saveeat.features.leaderboard.presentation.state

import com.mattiamularoni.saveeat.features.leaderboard.presentation.LeaderboardUserUi

/**
 * Sealed class che rappresenta gli stati UI della leaderboard.
 *
 * States:
 * - Loading: caricamento iniziale
 * - Success: leaderboard caricata con lista utenti
 * - Error: errore durante il fetch
 */
sealed class LeaderboardUiState {
    data object Loading : LeaderboardUiState()

    data class Success(
        val users: List<LeaderboardUserUi>
    ) : LeaderboardUiState()

    data class Error(val message: String) : LeaderboardUiState()
}
