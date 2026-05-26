package com.mattiamularoni.saveeat.features.auth.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.LoginRoute
import com.mattiamularoni.saveeat.features.auth.presentation.ui.LoginScreen

fun NavGraphBuilder.authScreen(
    onNavigateToPantry: () -> Unit = {}
) {
    composable<LoginRoute> {
        LoginScreen(onNavigateToPantry = onNavigateToPantry)
    }
}
