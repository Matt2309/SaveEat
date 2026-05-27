package com.mattiamularoni.saveeat.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mattiamularoni.saveeat.core.ui.MainScaffold
import com.mattiamularoni.saveeat.features.auth.presentation.navigation.authScreen
import com.mattiamularoni.saveeat.features.home.presentation.navigation.homeScreen
import com.mattiamularoni.saveeat.features.leaderboard.presentation.navigation.leaderboardScreen
import com.mattiamularoni.saveeat.features.pantry.presentation.navigation.pantryScreen
import com.mattiamularoni.saveeat.features.recipes.presentation.navigation.recipeScreen
import com.mattiamularoni.saveeat.features.recipes.presentation.navigation.recipeDetailScreen
import com.mattiamularoni.saveeat.features.scan_receipt.presentation.navigation.scanReceiptScreen


@Composable
fun SaveEatNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    MainScaffold(navController = navController) {
        NavHost(
            navController = navController,
            startDestination = LoginRoute,
            modifier = modifier
        ) {
            authScreen(
                onNavigateToPantry = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
            homeScreen(
                onNavigateToScan = {
                    navController.navigate(ScanReceiptRoute)
                }
            )
            pantryScreen(
                onNavigateToScan = {
                    navController.navigate(ScanReceiptRoute)
                }
            )
            recipeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
            recipeDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
            scanReceiptScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
            leaderboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

