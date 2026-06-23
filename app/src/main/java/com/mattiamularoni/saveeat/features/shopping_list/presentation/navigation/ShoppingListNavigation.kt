package com.mattiamularoni.saveeat.features.shopping_list.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mattiamularoni.saveeat.core.navigation.ShoppingListRoute
import com.mattiamularoni.saveeat.features.shopping_list.presentation.ui.ShoppingListScreen

fun NavGraphBuilder.shoppingListScreen(
    onNavigateBack: () -> Unit = {}
) {
    composable<ShoppingListRoute> {
        ShoppingListScreen(onNavigateBack = onNavigateBack)
    }
}
