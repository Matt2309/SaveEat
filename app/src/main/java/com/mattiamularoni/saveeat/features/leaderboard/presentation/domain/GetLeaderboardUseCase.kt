package com.mattiamularoni.saveeat.features.leaderboard.presentation.domain

import com.mattiamularoni.saveeat.features.leaderboard.domain.repository.LeaderboardRepository
import com.mattiamularoni.saveeat.features.leaderboard.domain.repository.LeaderboardUser as DomainLeaderboardUser
import com.mattiamularoni.saveeat.features.leaderboard.presentation.LeaderboardUserUi
import com.mattiamularoni.saveeat.features.leaderboard.presentation.RankingBadge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case per recuperare e trasformare dati leaderboard per la presentazione.
 *
 * Responsabilità:
 * - Fetch dati dal repository (domain model)
 * - Trasformazione domain model → UI model
 * - Aggiunta logica di ranking badges (Gold/Silver/Bronze)
 * - Formattazione punteggio e visualizzazione
 */
class GetLeaderboardUseCase(
    private val leaderboardRepository: LeaderboardRepository
) {

    /**
     * Richiama il repository e trasforma la leaderboard per la UI.
     *
     * @return Flow della lista leaderboard pronta per il rendering UI
     */
    operator fun invoke(): Flow<List<LeaderboardUserUi>> {
        return leaderboardRepository.observeLeaderboard().map { users ->
            users.mapIndexed { index, user -> user.toUiModel(index + 1) }
        }
    }

    private fun DomainLeaderboardUser.toUiModel(rank: Int): LeaderboardUserUi {
        val badge = when (rank) {
            1 -> RankingBadge.GOLD
            2 -> RankingBadge.SILVER
            3 -> RankingBadge.BRONZE
            else -> RankingBadge.NONE
        }

        return LeaderboardUserUi(
            id = id,
            displayName = displayName,
            avatarUrl = avatarUrl,
            ecoPoints = ecoPoints,
            rank = rank,
            formattedPoints = "${ecoPoints} pts",
            badge = badge
        )
    }
}
