package com.mattiamularoni.saveeat.features.pantry.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel.PantryViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PantryScreen(
    viewModel: PantryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val errorMessage = uiState.errorMessage

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            errorMessage != null -> Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge
            )

            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pantry items: ${uiState.items.size}",
                    style = MaterialTheme.typography.headlineSmall
                )
                // TODO (UI): Build detailed Material 3 layout here
            }
        }
    }
}
