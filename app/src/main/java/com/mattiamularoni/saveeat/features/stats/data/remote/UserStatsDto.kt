package com.mattiamularoni.saveeat.features.stats.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO per la risposta Postgrest di Supabase, tabella user_stats.
 */
@Serializable
data class UserStatsDto(
    @SerialName("user_id")
    val userId: String,
    @SerialName("total_kg_saved")
    val totalKgSaved: Double = 0.0,
    @SerialName("total_euros_saved")
    val totalEurosSaved: Double = 0.0
)
