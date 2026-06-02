package com.mattiamularoni.saveeat.features.recipes.domain.repository

/**
 * Modello di dominio per una ricetta preferita.
 *
 * Rappresenta la relazione many-to-many tra un utente e una ricetta
 * che l'utente ha aggiunto ai preferiti.
 */
data class FavoriteRecipe(
    val userId: String,
    val recipeId: String,
    val savedAt: Long
)
