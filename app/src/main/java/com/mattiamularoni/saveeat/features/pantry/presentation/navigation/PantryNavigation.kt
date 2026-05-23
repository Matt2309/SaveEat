package com.mattiamularoni.saveeat.features.pantry.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryScreen
import kotlinx.serialization.Serializable

@Serializable
object PantryRoute

fun NavGraphBuilder.pantryScreen() {
    composable<PantryRoute> {
        PantryScreen()
    }
}
