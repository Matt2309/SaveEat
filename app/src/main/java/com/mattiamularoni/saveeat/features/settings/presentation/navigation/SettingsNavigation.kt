package com.mattiamularoni.saveeat.features.settings.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.SettingsRoute
import com.mattiamularoni.saveeat.features.settings.presentation.ui.SettingsScreen

fun NavGraphBuilder.settingsScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    composable<SettingsRoute> {
        SettingsScreen(
            onNavigateBack = onNavigateBack,
            onLogout = onLogout,
        )
    }
}
