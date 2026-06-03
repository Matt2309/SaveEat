package com.mattiamularoni.saveeat.features.scan_receipt.presentation.viewmodel
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.usecase.ProcessReceiptUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScanReceiptUiState {
    object Idle : ScanReceiptUiState()
    object Loading : ScanReceiptUiState()
    object Success : ScanReceiptUiState()
    data class Error(val message: String) : ScanReceiptUiState()
}

class ScanReceiptViewModel(
    private val processReceiptUseCase: ProcessReceiptUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanReceiptUiState>(ScanReceiptUiState.Idle)
    val uiState: StateFlow<ScanReceiptUiState> = _uiState.asStateFlow()

    fun analyzeReceipt(bitmap: Bitmap) {
        _uiState.value = ScanReceiptUiState.Loading

        viewModelScope.launch {
            val result = processReceiptUseCase(bitmap)

            result.fold(
                onSuccess = {
                    _uiState.value = ScanReceiptUiState.Success
                },
                onFailure = { exception ->
                    val errorMessage = exception.message ?: "Errore sconosciuto durante la lettura"
                    _uiState.value = ScanReceiptUiState.Error(errorMessage)
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = ScanReceiptUiState.Idle
    }
}