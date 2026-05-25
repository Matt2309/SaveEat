package com.mattiamularoni.saveeat.features.pantry.presentation.state

import com.mattiamularoni.saveeat.features.pantry.presentation.PantryItem
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryCategory

sealed class PantryUiState {
    data object Loading : PantryUiState()

    data class Success(
        val items: List<PantryItem>,
        val selectedCategory: PantryCategory
    ) : PantryUiState()

    data class Error(val message: String) : PantryUiState()
}
