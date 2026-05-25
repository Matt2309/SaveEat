package com.mattiamularoni.saveeat.features.home.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.HomeRoute
import com.mattiamularoni.saveeat.features.home.presentation.ui.HomeScreen

fun NavGraphBuilder.homeScreen(
    onNavigateToScan: () -> Unit = {}
) {
    composable<HomeRoute> {
        HomeScreen(onNavigateToScan = onNavigateToScan)
    }
}
