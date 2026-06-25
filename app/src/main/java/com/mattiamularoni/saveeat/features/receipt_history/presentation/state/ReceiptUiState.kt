package com.mattiamularoni.saveeat.features.receipt_history.presentation.state

data class ReceiptUiState(
    val id: String,
    val storeName: String,
    val date: String,
    val totalPrice: String,
    val imageUrl: String,
)

data class ReceiptHistoryUiState(
    val receipts: List<ReceiptUiState> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
