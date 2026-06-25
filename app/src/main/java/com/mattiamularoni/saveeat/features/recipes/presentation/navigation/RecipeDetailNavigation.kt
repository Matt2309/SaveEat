package com.mattiamularoni.saveeat.features.recipes.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.RecipeDetailRoute
import com.mattiamularoni.saveeat.features.recipes.presentation.ui.RecipeDetailScreen

fun NavGraphBuilder.recipeDetailScreen(onNavigateBack: () -> Unit = {}) {
    composable<RecipeDetailRoute> { backStackEntry ->
        val id = backStackEntry.arguments?.getString("id") ?: ""
        RecipeDetailScreen(
            id = id,
            onNavigateBack = onNavigateBack,
        )
    }
}
