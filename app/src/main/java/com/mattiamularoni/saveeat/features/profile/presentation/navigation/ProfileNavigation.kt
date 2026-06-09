package com.mattiamularoni.saveeat.features.profile.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.ProfileRoute
import com.mattiamularoni.saveeat.features.profile.presentation.ui.ProfileScreen

fun NavGraphBuilder.profileScreen(
    onNavigateBack: () -> Unit = {}
) {
    composable<ProfileRoute> {
        ProfileScreen(onNavigateBack = onNavigateBack)
    }
}
