package com.mattiamularoni.saveeat.features.recipes.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO per la risposta Postgrest di Supabase.
 * Mapperà i campi JSON dello schema favorite_recipes table.
 */
@Serializable
data class FavoriteRecipeDto(
    @SerialName("user_id")
    val userId: String,
    @SerialName("recipe_id")
    val recipeId: String,
    @SerialName("saved_at")
    val savedAt: String,
)
