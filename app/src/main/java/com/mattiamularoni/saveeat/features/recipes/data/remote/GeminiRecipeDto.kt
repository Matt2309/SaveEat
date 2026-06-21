package com.mattiamularoni.saveeat.features.recipes.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRecipeDto(
    val title: String,
    val instructions: String,
    val ingredients: List<GeminiIngredientDto>,
    @SerialName("prep_time_minutes")
    val prepTimeMinutes: Int,
    val tags: List<String>,
    @SerialName("is_vegetarian")
    val isVegetarian: Boolean = false
)

@Serializable
data class GeminiIngredientDto(
    val name: String,
    val amount: Double = 1.0,
    val unit: String = "qb"
)
