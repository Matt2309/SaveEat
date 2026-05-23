package com.mattiamularoni.saveeat.features.pantry.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryCategory
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryTopBar() {
    CenterAlignedTopAppBar(
        title = { Text(text = "SaveEat") }
    )
}

@Composable
fun PantryBottomBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == "recipes",
            onClick = onNavigateToRecipes,
            icon = { Icon(Icons.Rounded.RestaurantMenu, contentDescription = null) },
            label = { Text("Ricette") }
        )
        NavigationBarItem(
            selected = currentRoute == "leaderboard",
            onClick = onNavigateToLeaderboard,
            icon = { Icon(Icons.Rounded.Leaderboard, contentDescription = null) },
            label = { Text("Classifica") }
        )
    }
}

@Composable
fun ScanFab(
    onNavigateToScan: () -> Unit,
    onNavigateToManualAdd: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FloatingActionButton(onClick = onNavigateToManualAdd) {
            Icon(Icons.Rounded.Add, contentDescription = null)
        }
        FloatingActionButton(onClick = onNavigateToScan) {
            Icon(Icons.Rounded.Search, contentDescription = null)
        }
    }
}

@Composable
fun CategoryFilterRow(
    selectedCategory: PantryCategory,
    onCategorySelected: (PantryCategory) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PantryCategory.entries.forEach { category ->
            AssistChip(
                onClick = { onCategorySelected(category) },
                label = { Text(categoryLabel(category)) },
                leadingIcon = {
                    if (selectedCategory == category) {
                        Icon(Icons.Rounded.ShoppingCart, contentDescription = null)
                    }
                }
            )
        }
    }
}

@Composable
fun PantrySection(
    title: String,
    icon: ImageVector,
    items: List<PantryItem>,
    onAddToShoppingList: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null)
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                }
                Text(text = "${items.size}")
            }

            items.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = item.name, style = MaterialTheme.typography.titleSmall)
                            Text(text = "${item.quantity} • ${item.expirationLabel}", style = MaterialTheme.typography.bodySmall)
                        }
                        AssistChip(
                            onClick = { onAddToShoppingList(item.id) },
                            label = { Text("Aggiungi") }
                        )
                    }
                }
            }
        }
    }
}

private fun categoryLabel(category: PantryCategory): String = when (category) {
    PantryCategory.ALL -> "Tutti"
    PantryCategory.FRIDGE -> "Frigo"
    PantryCategory.PANTRY -> "Dispensa"
    PantryCategory.FREEZER -> "Freezer"
}
