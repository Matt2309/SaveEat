package com.mattiamularoni.saveeat.features.receipt_history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.core.util.DateTimeUtils
import com.mattiamularoni.saveeat.features.receipt_history.domain.repository.Receipt
import com.mattiamularoni.saveeat.features.receipt_history.domain.usecase.GetReceiptHistoryUseCase
import com.mattiamularoni.saveeat.features.receipt_history.presentation.state.ReceiptHistoryUiState
import com.mattiamularoni.saveeat.features.receipt_history.presentation.state.ReceiptUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class ReceiptHistoryViewModel(
    private val getReceiptHistoryUseCase: GetReceiptHistoryUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReceiptHistoryUiState(isLoading = true))
    val uiState: StateFlow<ReceiptHistoryUiState> = _uiState.asStateFlow()

    init {
        loadReceipts()
    }

    fun loadReceipts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val receipts = getReceiptHistoryUseCase()
                _uiState.update {
                    it.copy(isLoading = false, receipts = receipts.map(::toUiState))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Errore nel caricamento degli scontrini")
                }
            }
        }
    }

    private fun toUiState(receipt: Receipt) =
        ReceiptUiState(
            id = receipt.id,
            storeName = receipt.storeName,
            date = DateTimeUtils.formatReceiptDisplayDate(receipt.scannedAt),
            totalPrice = "€ " + String.format(Locale.ITALY, "%.2f", receipt.totalPrice),
            imageUrl = receipt.imageUrl,
        )
}
