package com.mattiamularoni.saveeat.features.recipes.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO per la risposta Postgrest di Supabase.
 * Mapperà i campi JSON dello schema recipes table.
 */
@Serializable
data class RecipeDto(
    val id: String,
    val title: String,
    val instructions: String,
    val ingredients: String,
    @SerialName("prep_time_minutes")
    val prepTimeMinutes: Int,
    val tags: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("is_vegetarian")
    val isVegetarian: Boolean = false
)
