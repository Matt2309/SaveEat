package com.mattiamularoni.saveeat.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mattiamularoni.saveeat.core.ui.MainScaffold
import com.mattiamularoni.saveeat.features.auth.presentation.navigation.authScreen
import com.mattiamularoni.saveeat.features.auth.presentation.navigation.biometricScreen
import com.mattiamularoni.saveeat.features.auth.presentation.viewmodel.AuthViewModel
import com.mattiamularoni.saveeat.features.home.presentation.navigation.homeScreen
import com.mattiamularoni.saveeat.features.leaderboard.presentation.navigation.leaderboardScreen
import com.mattiamularoni.saveeat.features.pantry.presentation.navigation.pantryScreen
import com.mattiamularoni.saveeat.features.recipes.presentation.navigation.recipeDetailScreen
import com.mattiamularoni.saveeat.features.recipes.presentation.navigation.recipeScreen
import com.mattiamularoni.saveeat.features.scan_receipt.presentation.navigation.scanReceiptScreen
import io.github.jan.supabase.auth.status.SessionStatus
import org.koin.androidx.compose.koinViewModel

@Composable
fun SaveEatNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = koinViewModel()
    val sessionStatus by authViewModel.sessionStatus.collectAsState()

    val biometricRequired by authViewModel.biometricRequired.collectAsState()

    // Blocca la transizione immediata a HomeRoute se l'utente ha il dialog aperto
    val showBiometricProposal by authViewModel.showBiometricProposal.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                authViewModel.onAppForeground()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(sessionStatus, biometricRequired, showBiometricProposal) {
        val currentDestination = navController.currentDestination?.route
        val isOnLoginRoute = currentDestination == LoginRoute::class.qualifiedName ||
                currentDestination == "LoginRoute"
        val isOnBiometricRoute = currentDestination?.contains(
            BiometricRoute::class.qualifiedName.orEmpty()
        ) == true

        val bioRequired = biometricRequired ?: return@LaunchedEffect

        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                when {
                    bioRequired && !isOnBiometricRoute -> {
                        navController.navigate(BiometricRoute) {
                            if (isOnLoginRoute) popUpTo(LoginRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    !bioRequired && isOnLoginRoute && !showBiometricProposal -> {
                        // Naviga a Home SOLO se l'utente non deve visualizzare il dialog biometrico
                        navController.navigate(HomeRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
            is SessionStatus.NotAuthenticated -> {
                if (!isOnLoginRoute) {
                    navController.navigate(LoginRoute) {
                        popUpTo(0)
                    }
                }
            }
            else -> { /* SessionStatus.Loading o NetworkError: attendiamo */ }
        }
    }

    MainScaffold(navController = navController) {
        NavHost(
            navController = navController,
            startDestination = LoginRoute,
            modifier = modifier
        ) {
            authScreen(
                authViewModel = authViewModel,
                onNavigateToPantry = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )

            biometricScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(HomeRoute) {
                        popUpTo(0) { inclusive = true }
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