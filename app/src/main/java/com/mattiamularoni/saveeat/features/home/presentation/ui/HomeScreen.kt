package com.mattiamularoni.saveeat.features.home.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mattiamularoni.saveeat.features.home.presentation.viewmodel.HomeViewModel
import com.mattiamularoni.saveeat.features.pantry.presentation.components.ExpandableFab
import com.mattiamularoni.saveeat.features.pantry.presentation.components.ManualItemFormDialog
import org.koin.androidx.compose.koinViewModel
import com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel.PantryViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToScan: () -> Unit = {},
    pantryViewModel: PantryViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel()
) {
    var showManualForm by remember { mutableStateOf(false) }

    if (showManualForm) {
        ManualItemFormDialog(
            onDismiss = { showManualForm = false },
            onSubmit = { formState ->
                pantryViewModel.onManualItemInsert(formState)
                showManualForm = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExpandableFab(
                onScannerClick = onNavigateToScan,
                onManualInsertClick = { showManualForm = true }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ciao, ${homeViewModel.currentUserName}!",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "ID: ${homeViewModel.currentUserId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
