package com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryCategory
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryItem
import com.mattiamularoni.saveeat.features.pantry.presentation.domain.GetPantryItemsUseCase
import com.mattiamularoni.saveeat.features.pantry.presentation.state.PantryUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed interface PantryEffect {
    data class ShowSnackbar(val message: String) : PantryEffect
}

class PantryViewModel(
    private val getPantryItemsUseCase: GetPantryItemsUseCase
) : ViewModel() {
    private val allItems = MutableStateFlow<List<PantryItem>>(emptyList())
    private val _uiState = MutableStateFlow<PantryUiState>(PantryUiState.Loading)
    private val _effects = MutableSharedFlow<PantryEffect>()

    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()
    val effects: SharedFlow<PantryEffect> = _effects.asSharedFlow()

    private var hasLoaded = false
    private var errorMessage: String? = null
    private var selectedCategory = PantryCategory.ALL

    init {
        observePantryItems()
    }

    fun onCategorySelected(category: PantryCategory) {
        selectedCategory = category
        publishState()
    }

    fun onAddToShoppingList(itemId: String) {
        val item = allItems.value.firstOrNull { it.id == itemId }
        viewModelScope.launch {
            if (item == null) {
                _effects.emit(PantryEffect.ShowSnackbar("Elemento non trovato"))
            } else {
                _effects.emit(PantryEffect.ShowSnackbar("${item.name} aggiunto alla lista della spesa"))
            }
        }
    }

    private fun observePantryItems() {
        viewModelScope.launch {
            getPantryItemsUseCase()
                .onEach { items ->
                    hasLoaded = true
                    errorMessage = null
                    allItems.value = items
                    publishState()
                }
                .catch { throwable ->
                    hasLoaded = true
                    errorMessage = throwable.message ?: "Unable to load pantry items"
                    publishState()
                }
                .collect()
        }
    }

    private fun publishState() {
        _uiState.value = when {
            errorMessage != null -> PantryUiState.Error(errorMessage.orEmpty())
            !hasLoaded -> PantryUiState.Loading
            else -> PantryUiState.Success(
                items = filterItems(allItems.value, selectedCategory),
                selectedCategory = selectedCategory
            )
        }
    }

    private fun filterItems(
        items: List<PantryItem>,
        category: PantryCategory
    ): List<PantryItem> {
        return if (category == PantryCategory.ALL) {
            items
        } else {
            items.filter { it.category == category }
        }
    }
}
