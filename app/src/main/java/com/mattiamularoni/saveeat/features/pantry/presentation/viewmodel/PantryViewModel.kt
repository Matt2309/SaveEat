package com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryCategory
import com.mattiamularoni.saveeat.features.pantry.presentation.PantryItem
import com.mattiamularoni.saveeat.features.pantry.presentation.components.ManualItemFormState
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
import java.util.UUID

sealed interface PantryEffect {
    data class ShowSnackbar(val message: String) : PantryEffect
}

class PantryViewModel(
    private val getPantryItemsUseCase: GetPantryItemsUseCase,
    private val pantryRepository: PantryRepository,
    private val sessionProvider: SessionProvider
) : ViewModel() {
    private val allItems = MutableStateFlow<List<PantryItem>>(emptyList())
    private val _uiState = MutableStateFlow<PantryUiState>(PantryUiState.Loading)
    private val _effects = MutableSharedFlow<PantryEffect>()

    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()
    val effects: SharedFlow<PantryEffect> = _effects.asSharedFlow()

    private var selectedCategory = PantryCategory.ALL

    init {
        observePantryItems()
        viewModelScope.launch {
            try {
                pantryRepository.syncPantry()
            } catch (e: Exception) {
                android.util.Log.e("PantryViewModel", "Sync fallito: ${e.message}", e)
                _effects.emit(PantryEffect.ShowSnackbar("Sincronizzazione non riuscita. Dati locali mostrati."))
            }
        }
    }

    fun onCategorySelected(category: PantryCategory) {
        selectedCategory = category
        if (_uiState.value is PantryUiState.Success) {
            _uiState.value = PantryUiState.Success(
                items = filterItems(allItems.value, selectedCategory),
                selectedCategory = selectedCategory
            )
        }
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

    fun onManualItemInsert(formState: ManualItemFormState) {
        viewModelScope.launch {
            try {
                val categoryString = when (formState.category) {
                    PantryCategory.FRIDGE -> "FRIDGE"
                    PantryCategory.PANTRY -> "PANTRY"
                    PantryCategory.FREEZER -> "FREEZER"
                    else -> "PANTRY"
                }

                val expirationDate = formState.expirationDate?.let {
                    java.time.Instant.from(
                        it.atStartOfDay(java.time.ZoneId.systemDefault())
                    ).toEpochMilli()
                }

                val newItem = com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryItem(
                    id = UUID.randomUUID().toString(),
                    userId = sessionProvider.getCurrentUserId(),
                    receiptId = null,
                    name = formState.itemName,
                    category = categoryString,
                    isPlaceholder = false,
                    status = "active",
                    quantity = if (formState.quantity.isNotEmpty()) formState.quantity.toDoubleOrNull() ?: 0.0 else 0.0,
                    unit = if (formState.unit.isNotEmpty()) formState.unit else null,
                    expirationDate = expirationDate
                )

                val itemId = pantryRepository.addPantryItem(newItem)
                _effects.emit(PantryEffect.ShowSnackbar("${formState.itemName} aggiunto con successo"))
            } catch (e: Exception) {
                _effects.emit(PantryEffect.ShowSnackbar("Errore nell'aggiunta dell'elemento: ${e.message}"))
            }
        }
    }

    private fun observePantryItems() {
        viewModelScope.launch {
            getPantryItemsUseCase()
                .onEach { items ->
                    allItems.value = items
                    _uiState.value = PantryUiState.Success(
                        items = filterItems(items, selectedCategory),
                        selectedCategory = selectedCategory
                    )
                }
                .catch { throwable ->
                    _uiState.value = PantryUiState.Error(throwable.message ?: "Unable to load pantry items")
                }
                .collect()
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
