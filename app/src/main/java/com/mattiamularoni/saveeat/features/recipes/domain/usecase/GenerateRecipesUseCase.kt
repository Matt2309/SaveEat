package com.mattiamularoni.saveeat.features.recipes.domain.usecase

import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.recipes.domain.model.RecipeFilters
import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe
import com.mattiamularoni.saveeat.features.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first

class GenerateRecipesUseCase(
    private val pantryRepository: PantryRepository,
    private val recipeRepository: RecipeRepository,
) {
    suspend operator fun invoke(filters: RecipeFilters): Result<List<Recipe>> =
        try {
            val expiringItems = pantryRepository.getExpiringItems(7).first()
            val ingredients = expiringItems.map { it.name }
            val preferences = buildPreferencesMap(filters)
            val recipes = recipeRepository.getSuggestedRecipesForExpiringItems(ingredients, preferences)
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun buildPreferencesMap(filters: RecipeFilters): Map<String, Any> {
        val prefs = mutableMapOf<String, Any>()
        filters.cuisineStyle?.let { prefs["cuisine_style"] = it }
        filters.timingPreference?.let { prefs["timing"] = it }
        if (filters.vegetarian) prefs["vegetarian"] = true
        return prefs
    }
}
