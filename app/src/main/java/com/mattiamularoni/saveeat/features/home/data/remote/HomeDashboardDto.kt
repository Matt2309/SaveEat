package com.mattiamularoni.saveeat.features.home.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO per la risposta aggregata della dashboard Home da Supabase.
 *
 * Rappresenta il payload completo della dashboard che combina:
 * - Item in scadenza dalla pantry
 * - Ranking leaderboard
 * - Suggerimenti ricette
 * - Statistiche utente
 * - Profilo utente
 */
@Serializable
data class HomeDashboardDto(
    @SerialName("user_id")
    val userId: String,
    @SerialName("expiring_items")
    val expiringItems: List<ExpiringItemDto> = emptyList(),
    @SerialName("top_leaderboard")
    val topLeaderboard: List<LeaderboardUserDto> = emptyList(),
    @SerialName("suggested_recipes")
    val suggestedRecipes: List<SuggestedRecipeDto> = emptyList(),
    @SerialName("user_stats")
    val userStats: UserStatsDto = UserStatsDto(),
    @SerialName("user_profile")
    val userProfile: UserProfileDto = UserProfileDto(),
    @SerialName("last_synced_at")
    val lastSyncedAt: String? = null
)

/**
 * DTO per un elemento della pantry in scadenza.
 *
 * Include solo i campi rilevanti per la visualizzazione nella dashboard.
 */
@Serializable
data class ExpiringItemDto(
    val id: String,
    val name: String,
    val category: String,
    @SerialName("category_key")
    val categoryKey: String? = null,
    @SerialName("expiration_date")
    val expirationDate: String,
    val quantity: Double? = null,
    val unit: String? = null
)

/**
 * DTO per un utente nel leaderboard.
 *
 * Include dati ranking e profilo essenziali per la snippet leaderboard.
 */
@Serializable
data class LeaderboardUserDto(
    val id: String = "",
    val email: String = "",
    @SerialName("display_name")
    val displayName: String = "",
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("eco_points")
    val ecoPoints: Int = 0
)

/**
 * DTO per una ricetta suggerita.
 *
 * Include metadati essenziali per la card ricetta nella dashboard.
 */
@Serializable
data class SuggestedRecipeDto(
    val id: String,
    val title: String,
    val tags: String? = null,
    @SerialName("prep_time_minutes")
    val prepTimeMinutes: Int? = null,
    @SerialName("matching_ingredients")
    val matchingIngredients: Int = 0 // Numero ingredienti che matchano con expiring items
)

/**
 * DTO per le statistiche aggregate della pantry.
 *
 * Contiene metriche di sintesi della dispensa dell'utente.
 */
@Serializable
data class UserStatsDto(
    @SerialName("total_items")
    val totalItems: Int = 0,
    @SerialName("expiring_count")
    val expiringCount: Int = 0,
    @SerialName("active_placeholders")
    val activePlaceholders: Int = 0,
    @SerialName("eco_points")
    val ecoPoints: Int = 0
)

/**
 * DTO per i dati profilo dell'utente.
 *
 * Include informazioni di identità e ranking per la home.
 */
@Serializable
data class UserProfileDto(
    val id: String = "",
    @SerialName("display_name")
    val displayName: String = "",
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("rank_position")
    val rankPosition: Int = 0
)
