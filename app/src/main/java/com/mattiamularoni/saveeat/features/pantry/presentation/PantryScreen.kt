package com.mattiamularoni.saveeat.features.pantry.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mattiamularoni.saveeat.features.pantry.presentation.components.CategoryFilterRow
import com.mattiamularoni.saveeat.features.pantry.presentation.components.ExpandableFab
import com.mattiamularoni.saveeat.features.pantry.presentation.components.ManualItemFormDialog
import com.mattiamularoni.saveeat.features.pantry.presentation.components.PantrySection
import com.mattiamularoni.saveeat.features.pantry.presentation.components.PantryTopBar
import com.mattiamularoni.saveeat.features.pantry.presentation.state.PantryUiState
import com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel.PantryEffect
import com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel.PantryViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel = koinViewModel(),
    onNavigateToScan: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showManualForm by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PantryEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    if (showManualForm) {
        ManualItemFormDialog(
            onDismiss = { showManualForm = false },
            onSubmit = { formState ->
                viewModel.onManualItemInsert(formState)
                showManualForm = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Insets già gestiti da MainScaffold: evitiamo il doppio inset
        // (striscia bianca in basso + FAB troppo in alto).
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { PantryTopBar(onAvatarClick = onNavigateToProfile) },
        floatingActionButton = {
            ExpandableFab(
                onScannerClick = onNavigateToScan,
                onManualInsertClick = { showManualForm = true }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            PantryUiState.Loading -> LoadingPantryContent(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            )

            is PantryUiState.Success -> PantryContent(
                state = state,
                modifier = Modifier.padding(padding),
                onCategorySelected = viewModel::onCategorySelected,
                onAddToShoppingList = viewModel::onAddToShoppingList
            )

            is PantryUiState.Error -> ErrorContent(
                message = state.message,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun PantryContent(
    state: PantryUiState.Success,
    onCategorySelected: (PantryCategory) -> Unit,
    onAddToShoppingList: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = listOf(
        PantrySectionSpec(
            category = PantryCategory.FRIDGE,
            title = "Frigo",
            icon = Icons.Rounded.Kitchen,
            items = state.items.filter { it.category == PantryCategory.FRIDGE }
        ),
        PantrySectionSpec(
            category = PantryCategory.PANTRY,
            title = "Dispensa",
            icon = Icons.Rounded.Inventory2,
            items = state.items.filter { it.category == PantryCategory.PANTRY }
        ),
        PantrySectionSpec(
            category = PantryCategory.FREEZER,
            title = "Freezer",
            icon = Icons.Rounded.AcUnit,
            items = state.items.filter { it.category == PantryCategory.FREEZER }
        )
    )
    val visibleSections = when (state.selectedCategory) {
        PantryCategory.ALL -> sections.filter { it.items.isNotEmpty() }
        else -> sections.filter { it.category == state.selectedCategory && it.items.isNotEmpty() }
    }

    if (visibleSections.isEmpty()) {
        EmptyContent(modifier = modifier.fillMaxSize())
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CategoryFilterRow(
                selectedCategory = state.selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }
        items(visibleSections) { section ->
            PantrySection(
                title = section.title,
                icon = section.icon,
                items = section.items,
                assets = state.assets,
                onAddToShoppingList = onAddToShoppingList
            )
        }
    }
}

@Composable
private fun LoadingPantryContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp)
                    .padding(horizontal = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.Inventory2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "Dispensa vuota",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Aggiungi prodotti con lo scanner o manualmente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class PantrySectionSpec(
    val category: PantryCategory,
    val title: String,
    val icon: ImageVector,
    val items: List<PantryItem>
)
