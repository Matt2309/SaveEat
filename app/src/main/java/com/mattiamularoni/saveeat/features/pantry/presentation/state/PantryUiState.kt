package com.mattiamularoni.saveeat.features.pantry.presentation.state

import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryItem

data class PantryUiState(
    val isLoading: Boolean = true,
    val items: List<PantryItem> = emptyList(),
    val errorMessage: String? = null
)
