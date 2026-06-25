package com.mattiamularoni.saveeat.features.leaderboard.presentation

import com.mattiamularoni.saveeat.features.leaderboard.domain.model.EcoTitle

/**
 * Enum per i badge di ranking (top 3 utenti).
 */
enum class RankingBadge {
    GOLD,
    SILVER,
    BRONZE,
    NONE,
}

/**
 * Modello di presentazione per un utente nella leaderboard.
 *
 * Contiene dati formattati e pronti per il rendering UI.
 */
data class LeaderboardUserUi(
    val id: String,
    val displayName: String,
    val avatarUrl: String?,
    val ecoPoints: Int,
    val rank: Int,
    val formattedPoints: String,
    val badge: RankingBadge,
    val ecoTitle: EcoTitle,
)
