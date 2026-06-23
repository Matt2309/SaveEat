package com.mattiamularoni.saveeat.features.shopping_list.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.features.shopping_list.domain.usecase.AddToShoppingListUseCase
import com.mattiamularoni.saveeat.features.shopping_list.domain.usecase.ClearShoppingListUseCase
import com.mattiamularoni.saveeat.features.shopping_list.domain.usecase.GetShoppingListUseCase
import com.mattiamularoni.saveeat.features.shopping_list.domain.usecase.RemoveFromShoppingListUseCase
import com.mattiamularoni.saveeat.features.shopping_list.presentation.state.ShoppingListUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed interface ShoppingListEffect {
    data class ShowSnackbar(val message: String) : ShoppingListEffect
}

class ShoppingListViewModel(
    private val getShoppingListUseCase: GetShoppingListUseCase,
    private val addToShoppingListUseCase: AddToShoppingListUseCase,
    private val removeFromShoppingListUseCase: RemoveFromShoppingListUseCase,
    private val clearShoppingListUseCase: ClearShoppingListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShoppingListUiState>(ShoppingListUiState.Loading)
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ShoppingListEffect>()
    val effects: SharedFlow<ShoppingListEffect> = _effects.asSharedFlow()

    init {
        observeShoppingList()
    }

    private fun observeShoppingList() {
        getShoppingListUseCase()
            .onEach { items -> _uiState.value = ShoppingListUiState.Success(items) }
            .catch { e ->
                _uiState.value = ShoppingListUiState.Error(
                    e.message ?: "Errore nel caricamento della lista della spesa"
                )
            }
            .launchIn(viewModelScope)
    }

    fun addItem(name: String) {
        viewModelScope.launch {
            addToShoppingListUseCase(name).onFailure { e ->
                _effects.emit(ShoppingListEffect.ShowSnackbar(e.message ?: "Errore nell'aggiunta"))
            }
        }
    }

    fun removeItem(id: String) {
        viewModelScope.launch {
            removeFromShoppingListUseCase(id).onFailure { e ->
                _effects.emit(ShoppingListEffect.ShowSnackbar(e.message ?: "Errore nella rimozione"))
            }
        }
    }

    fun clearList() {
        viewModelScope.launch {
            clearShoppingListUseCase().fold(
                onSuccess = { _effects.emit(ShoppingListEffect.ShowSnackbar("Lista della spesa svuotata")) },
                onFailure = { e -> _effects.emit(ShoppingListEffect.ShowSnackbar(e.message ?: "Errore")) }
            )
        }
    }
}
