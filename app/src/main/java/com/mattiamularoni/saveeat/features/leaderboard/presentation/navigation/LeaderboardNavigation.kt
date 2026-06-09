package com.mattiamularoni.saveeat.features.leaderboard.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.LeaderboardRoute
import com.mattiamularoni.saveeat.features.leaderboard.presentation.ui.LeaderboardScreen

fun NavGraphBuilder.leaderboardScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    composable<LeaderboardRoute> {
        LeaderboardScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToProfile = onNavigateToProfile
        )
    }
}
