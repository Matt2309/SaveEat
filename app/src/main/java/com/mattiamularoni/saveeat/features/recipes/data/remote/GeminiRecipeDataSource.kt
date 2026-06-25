package com.mattiamularoni.saveeat.features.recipes.data.remote

interface GeminiRecipeDataSource {
    suspend fun generateRecipes(
        ingredients: List<String>,
        preferences: Map<String, Any>,
    ): String
}
