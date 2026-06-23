package com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.pantry.domain.model.PantryAsset
import com.mattiamularoni.saveeat.features.stats.domain.repository.StatsRepository
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryAssetRepository
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.pantry.presentation.FreshnessLevel
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface PantryEffect {
    data class ShowSnackbar(val message: String) : PantryEffect
}

class PantryViewModel(
    private val getPantryItemsUseCase: GetPantryItemsUseCase,
    private val pantryRepository: PantryRepository,
    private val pantryAssetRepository: PantryAssetRepository,
    private val sessionProvider: SessionProvider,
    private val statsRepository: StatsRepository
) : ViewModel() {
    private val allItems = MutableStateFlow<List<PantryItem>>(emptyList())
    // Id rimossi in modo "ottimistico" lato UI: spariscono subito, prima che la
    // cancellazione async sul repository propaghi al flow.
    private val _removedIds = MutableStateFlow<Set<String>>(emptySet())
    private val _uiState = MutableStateFlow<PantryUiState>(PantryUiState.Loading)
    private val _effects = MutableSharedFlow<PantryEffect>()

    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()
    val effects: SharedFlow<PantryEffect> = _effects.asSharedFlow()

    private var selectedCategory = PantryCategory.ALL

    init {
        observeState()
        viewModelScope.launch {
            try {
                pantryRepository.syncPantry()
            } catch (e: Exception) {
                android.util.Log.e("PantryViewModel", "Sync fallito: ${e.message}", e)
                _effects.emit(PantryEffect.ShowSnackbar("Sincronizzazione non riuscita. Dati locali mostrati."))
            }
        }
        viewModelScope.launch {
            try {
                pantryAssetRepository.syncAssets()
            } catch (e: Exception) {
                android.util.Log.e("PantryViewModel", "Asset sync fallito: ${e.message}")
            }
        }
    }

    fun onCategorySelected(category: PantryCategory) {
        selectedCategory = category
        val current = _uiState.value
        if (current is PantryUiState.Success) {
            _uiState.value = current.copy(
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

    /** Swipe → destra: elimina il prodotto dalla dispensa. */
    fun onDeleteItem(itemId: String) {
        _removedIds.value = _removedIds.value + itemId // sparisce subito dalla UI
        viewModelScope.launch {
            try {
                pantryRepository.deletePantryItem(itemId)
                _effects.emit(PantryEffect.ShowSnackbar("Prodotto eliminato"))
            } catch (e: Exception) {
                _removedIds.value = _removedIds.value - itemId // ripristina se fallisce
                _effects.emit(PantryEffect.ShowSnackbar("Errore nell'eliminazione: ${e.message}"))
            }
        }
    }

    /**
     * Swipe ← sinistra: segna come consumato (rimuove dalla dispensa).
     * Se il prodotto è in scadenza assegna +[CONSUME_POINTS] Eco-punti.
     */
    fun onConsumeItem(itemId: String) {
        val item = allItems.value.firstOrNull { it.id == itemId }
        val expiring = item?.freshnessLevel == FreshnessLevel.CRITICAL ||
                item?.freshnessLevel == FreshnessLevel.MEDIUM
        _removedIds.value += itemId // sparisce subito dalla UI
        viewModelScope.launch {
            try {
                // "Consumato" = rimosso dalla dispensa.
                pantryRepository.deletePantryItem(itemId)

                // Se in scadenza, assegna davvero gli eco-punti all'utente.
                if (expiring) {
                    statsRepository.addRecipeCookedStats(kg = 0.0, euros = 0.0, points = CONSUME_POINTS)
                        .onFailure {
                            android.util.Log.e("PantryViewModel", "Aggiornamento eco-punti fallito: ${it.message}")
                        }
                }

                val msg = if (expiring) {
                    "${item?.name ?: "Prodotto"} consumato! +$CONSUME_POINTS Eco-punti"
                } else {
                    "${item?.name ?: "Prodotto"} segnato come consumato"
                }
                _effects.emit(PantryEffect.ShowSnackbar(msg))
            } catch (e: Exception) {
                _removedIds.value = _removedIds.value - itemId // ripristina se fallisce
                _effects.emit(PantryEffect.ShowSnackbar("Errore: ${e.message}"))
            }
        }
    }

    private companion object {
        const val CONSUME_POINTS = 5
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
                    categoryKey = "",
                    isPlaceholder = false,
                    status = "ACTIVE",
                    quantity = if (formState.quantity.isNotEmpty()) formState.quantity.toDoubleOrNull() ?: 0.0 else 0.0,
                    unit = if (formState.unit.isNotEmpty()) formState.unit else null,
                    expirationDate = expirationDate
                )

                pantryRepository.addPantryItem(newItem)
                _effects.emit(PantryEffect.ShowSnackbar("${formState.itemName} aggiunto con successo"))
            } catch (e: Exception) {
                _effects.emit(PantryEffect.ShowSnackbar("Errore nell'aggiunta dell'elemento: ${e.message}"))
            }
        }
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                getPantryItemsUseCase(),
                pantryAssetRepository.observeAssets(),
                _removedIds
            ) { items: List<PantryItem>, assets: Map<String, PantryAsset>, removed: Set<String> ->
                val visible = items.filterNot { it.id in removed }
                allItems.value = visible
                PantryUiState.Success(
                    items = filterItems(visible, selectedCategory),
                    assets = assets,
                    selectedCategory = selectedCategory
                )
            }
                .catch { throwable ->
                    _uiState.value = PantryUiState.Error(throwable.message ?: "Unable to load pantry items")
                }
                .collect { _uiState.value = it }
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
