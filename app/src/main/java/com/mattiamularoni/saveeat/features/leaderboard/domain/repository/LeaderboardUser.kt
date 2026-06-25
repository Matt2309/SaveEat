package com.mattiamularoni.saveeat.features.leaderboard.domain.repository

/**
 * Modello di dominio per un utente nella classifica globale.
 *
 * Rappresenta un singolo utente con il suo punteggio eco_points
 * e la posizione nella leaderboard.
 */
data class LeaderboardUser(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val ecoPoints: Int,
    val rank: Int? = null,
)
