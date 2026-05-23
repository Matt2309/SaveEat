package com.mattiamularoni.saveeat.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.mattiamularoni.saveeat.features.pantry.presentation.navigation.PantryRoute
import com.mattiamularoni.saveeat.features.pantry.presentation.navigation.pantryScreen


@Composable
fun SaveEatNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PantryRoute,
        modifier = modifier
    ) {
        pantryScreen()
    }
}
