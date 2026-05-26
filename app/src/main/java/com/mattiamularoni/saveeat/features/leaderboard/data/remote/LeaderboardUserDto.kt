package com.mattiamularoni.saveeat.features.leaderboard.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO per la risposta Postgrest di Supabase.
 *
 * Mapperà i campi JSON dello schema users table
 * da cui si legge la classifica globale ordinata per eco_points.
 */
@Serializable
data class LeaderboardUserDto(
    val id: String,
    val email: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("eco_points")
    val ecoPoints: Int
)
