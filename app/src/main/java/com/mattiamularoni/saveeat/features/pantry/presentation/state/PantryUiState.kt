package com.mattiamularoni.saveeat.features.pantry.presentation.state

import com.mattiamularoni.saveeat.features.pantry.domain.model.PantryAsset
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryCategory
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryItem

sealed class PantryUiState {
    data object Loading : PantryUiState()

    data class Success(
        val items: List<PantryItem>,
        val assets: Map<String, PantryAsset> = emptyMap(),
        val selectedCategory: PantryCategory
    ) : PantryUiState()

    data class Error(val message: String) : PantryUiState()
}
