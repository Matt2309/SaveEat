package com.mattiamularoni.saveeat.features.shopping_list.presentation.state

import com.mattiamularoni.saveeat.features.shopping_list.domain.model.ShoppingListItem

sealed class ShoppingListUiState {
    data object Loading : ShoppingListUiState()
    data class Success(val items: List<ShoppingListItem> = emptyList()) : ShoppingListUiState()
    data class Error(val message: String) : ShoppingListUiState()
}
