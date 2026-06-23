package com.mattiamularoni.saveeat.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mattiamularoni.saveeat.core.navigation.HomeRoute
import com.mattiamularoni.saveeat.core.navigation.LeaderboardRoute
import com.mattiamularoni.saveeat.core.navigation.PantryRoute
import com.mattiamularoni.saveeat.core.navigation.RecipeDetailRoute
import com.mattiamularoni.saveeat.core.navigation.RecipeRoute
import com.mattiamularoni.saveeat.core.navigation.LoginRoute
import com.mattiamularoni.saveeat.core.navigation.ScanReceiptRoute

data class BottomNavigationItem(
    val route: Any,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navigationItems = listOf(
        BottomNavigationItem(HomeRoute, "Home", Icons.Outlined.Home),
        BottomNavigationItem(PantryRoute, "Pantry", Icons.Outlined.ShoppingBasket),
        BottomNavigationItem(RecipeRoute, "Recipes", Icons.Outlined.Restaurant),
        BottomNavigationItem(LeaderboardRoute, "Leaderboard", Icons.Outlined.EmojiEvents)
    )

    // Determine if bottom bar should be shown
    val isProfileRoute = currentDestination?.route?.contains("ProfileRoute") == true ||
            currentDestination?.route?.contains("SettingsRoute") == true
    val shouldShowBottomBar = (currentDestination?.let { destination ->
        navigationItems.any { item ->
            destination.hierarchy.any { navDest ->
                navDest.route?.contains(item.route::class.simpleName ?: "") == true
            }
        }
    } ?: false) || isProfileRoute

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    navigationItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { navDestination ->
                            navDestination.route?.contains(item.route::class.simpleName ?: "") == true
                        } ?: false

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(
                                text = item.label,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            ) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
}
