package com.mattiamularoni.saveeat.features.scan_receipt.presentation.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryItem
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.scan_receipt.domain.model.ParsedReceiptItem
import com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.usecase.AnalyzeReceiptUseCase
import com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.usecase.AnalyzedReceipt
import com.mattiamularoni.saveeat.features.scan_receipt.presentation.components.ReviewItemUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class ScanReceiptUiState {
    data object Idle : ScanReceiptUiState()
    data object Loading : ScanReceiptUiState()

    /** Carosello "Smart Expiry Date Review" sui prodotti freschi da confermare. */
    data class Review(
        val pendingItems: List<ReviewItemUiState>,
        val savedAutomaticallyCount: Int
    ) : ScanReceiptUiState()

    data class Error(val message: String) : ScanReceiptUiState()
}

/** Eventi one-shot consumati dalla UI (la navigazione resta callback-driven). */
sealed interface ScanReceiptEffect {
    data object NavigateToPantry : ScanReceiptEffect
}

class ScanReceiptViewModel(
    private val analyzeReceiptUseCase: AnalyzeReceiptUseCase,
    private val pantryRepository: PantryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanReceiptUiState>(ScanReceiptUiState.Idle)
    val uiState: StateFlow<ScanReceiptUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ScanReceiptEffect>()
    val effects: SharedFlow<ScanReceiptEffect> = _effects.asSharedFlow()

    /**
     * Dati grezzi dei prodotti deperibili in attesa di revisione, indicizzati per id.
     * Sorgente di categoria/quantità/unità al momento della conferma; i giorni selezionati
     * vivono invece nello stato [ScanReceiptUiState.Review] (single source of truth lato UI).
     */
    private val perishableById = mutableMapOf<String, ParsedReceiptItem>()

    /** Id dello scontrino registrato per lo scan corrente, usato per linkare i PantryItem salvati. */
    private var currentReceiptId: String? = null

    fun analyzeReceipt(bitmap: Bitmap) {
        _uiState.value = ScanReceiptUiState.Loading

        viewModelScope.launch {
            val result = analyzeReceiptUseCase(bitmap)

            result.fold(
                onSuccess = { analyzed -> onItemsParsed(analyzed) },
                onFailure = { exception ->
                    val errorMessage = exception.message ?: "Errore sconosciuto durante la lettura"
                    _uiState.value = ScanReceiptUiState.Error(errorMessage)
                }
            )
        }
    }

    private suspend fun onItemsParsed(analyzed: AnalyzedReceipt) {
        currentReceiptId = analyzed.receiptId
        val (perishable, nonPerishable) = analyzed.items.partition { it.isPerishable }

        // 1. Prodotti a lunga conservazione: salvataggio automatico (no attrito utente).
        if (nonPerishable.isNotEmpty()) {
            val pantryItems = nonPerishable.map { it.toPantryItem(expirationDate = null) }
            runCatching { pantryRepository.saveReceiptItems(analyzed.receiptId, pantryItems) }
                .onFailure { Log.e(TAG, "Salvataggio automatico non perishables fallito", it) }
        }

        // 2. Prodotti freschi: in memoria, da rivedere nel carosello.
        perishableById.clear()
        val reviewItems = perishable.map { item ->
            val id = UUID.randomUUID().toString()
            perishableById[id] = item
            ReviewItemUiState(
                id = id,
                title = item.name,
                imageUrl = "",
                aiSuggestedDays = item.estimatedExpiryDays,
                currentSelectedDays = item.estimatedExpiryDays
            )
        }

        if (reviewItems.isEmpty()) {
            // Niente da rivedere: chiudiamo il flow.
            _effects.emit(ScanReceiptEffect.NavigateToPantry)
        } else {
            _uiState.value = ScanReceiptUiState.Review(
                pendingItems = reviewItems,
                savedAutomaticallyCount = nonPerishable.size
            )
        }
    }

    fun onIncrementDays(itemId: String) = updateDays(itemId) { it + 1 }

    fun onDecrementDays(itemId: String) = updateDays(itemId) { (it - 1).coerceAtLeast(MIN_DAYS) }

    private inline fun updateDays(itemId: String, transform: (Int) -> Int) {
        val current = _uiState.value as? ScanReceiptUiState.Review ?: return
        val updated = current.pendingItems.map { item ->
            if (item.id == itemId) item.copy(currentSelectedDays = transform(item.currentSelectedDays))
            else item
        }
        _uiState.value = current.copy(pendingItems = updated)
    }

    fun onConfirmItem(itemId: String) {
        val parsed = perishableById[itemId] ?: return
        val days = (_uiState.value as? ScanReceiptUiState.Review)
            ?.pendingItems?.firstOrNull { it.id == itemId }
            ?.currentSelectedDays ?: parsed.estimatedExpiryDays

        val expiration = System.currentTimeMillis() + days.toLong() * MILLIS_PER_DAY
        savePantryItem(parsed.toPantryItem(expirationDate = expiration))
        removeItem(itemId)
    }

    fun onSkipItem(itemId: String) {
        val parsed = perishableById[itemId] ?: return
        // Lunga conservazione: expirationDate = null (la dispensa mostra "Lunga conservazione").
        savePantryItem(parsed.toPantryItem(expirationDate = null))
        removeItem(itemId)
    }

    fun onDismiss() {
        // Salviamo i freschi rimasti come "lunga conservazione" così nulla viene perso.
        val remaining = (_uiState.value as? ScanReceiptUiState.Review)?.pendingItems.orEmpty()
        remaining.forEach { item ->
            perishableById[item.id]?.let { savePantryItem(it.toPantryItem(expirationDate = null)) }
        }
        perishableById.clear()
        viewModelScope.launch { _effects.emit(ScanReceiptEffect.NavigateToPantry) }
    }

    fun resetState() {
        perishableById.clear()
        currentReceiptId = null
        _uiState.value = ScanReceiptUiState.Idle
    }

    private fun savePantryItem(item: PantryItem) {
        viewModelScope.launch {
            runCatching { pantryRepository.addPantryItem(item) }
                .onFailure { Log.e(TAG, "Salvataggio prodotto fresco fallito: ${item.name}", it) }
        }
    }

    private fun removeItem(itemId: String) {
        perishableById.remove(itemId)
        val current = _uiState.value as? ScanReceiptUiState.Review ?: return
        val remaining = current.pendingItems.filterNot { it.id == itemId }

        if (remaining.isEmpty()) {
            viewModelScope.launch { _effects.emit(ScanReceiptEffect.NavigateToPantry) }
        } else {
            _uiState.value = current.copy(pendingItems = remaining)
        }
    }

    private fun ParsedReceiptItem.toPantryItem(expirationDate: Long?): PantryItem = PantryItem(
        id = UUID.randomUUID().toString(),
        userId = "", // sovrascritto dal repository con l'utente corrente
        receiptId = currentReceiptId,
        name = name,
        category = category,
        categoryKey = categoryKey,
        isPlaceholder = false,
        status = "ACTIVE",
        quantity = quantity,
        unit = unit,
        expirationDate = expirationDate
    )

    private companion object {
        const val TAG = "ScanReceiptViewModel"
        const val MIN_DAYS = 1
        const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
    }
}
