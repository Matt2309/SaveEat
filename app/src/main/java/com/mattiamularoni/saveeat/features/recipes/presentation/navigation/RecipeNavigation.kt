package com.mattiamularoni.saveeat.features.recipes.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.RecipeRoute
import com.mattiamularoni.saveeat.features.recipes.presentation.ui.RecipeScreen

fun NavGraphBuilder.recipeScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    composable<RecipeRoute> {
        RecipeScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToProfile = onNavigateToProfile
        )
    }
}
