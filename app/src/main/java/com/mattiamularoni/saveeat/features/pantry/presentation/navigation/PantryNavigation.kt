package com.mattiamularoni.saveeat.features.pantry.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.PantryRoute
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryScreen

fun NavGraphBuilder.pantryScreen(
    onNavigateToScan: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    composable<PantryRoute> {
        PantryScreen(
            onNavigateToScan = onNavigateToScan,
            onNavigateToProfile = onNavigateToProfile,
        )
    }
}
