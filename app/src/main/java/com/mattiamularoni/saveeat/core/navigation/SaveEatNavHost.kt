package com.mattiamularoni.saveeat.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel
import io.github.jan.supabase.auth.status.SessionStatus


@Composable
fun SaveEatNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = koinViewModel()
    val sessionStatus by authViewModel.sessionStatus.collectAsState()

    LaunchedEffect(sessionStatus) {
        val currentDestination = navController.currentDestination?.route
        // Controlliamo se siamo sulla rotta di Login
        val isOnLoginRoute = currentDestination == LoginRoute::class.qualifiedName || currentDestination == "LoginRoute"

        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                if (isOnLoginRoute) {
                    // Utente loggato ma è sul Login: caccialo sulla Home!
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            }
            is SessionStatus.NotAuthenticated -> {
                if (!isOnLoginRoute) {
                    // Utente NON loggato ma cerca di vedere l'app: caccialo sul Login!
                    navController.navigate(LoginRoute) {
                        popUpTo(0) // Pulisce tutto lo stack
                    }
                }
            }
            else -> { /* Loading o NetworkError, non facciamo nulla */ }
        }
    }

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
                },
                onNavigateToPantry = {
                    navController.navigate(PantryRoute) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToRecipes = {
                    navController.navigate(RecipeRoute) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
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

