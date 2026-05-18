package com.mattiamularoni.saveeat.features.pantry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import com.mattiamularoni.saveeat.features.pantry.presentation.state.PantryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PantryViewModel(
    private val pantryRepository: PantryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    init {
        observePantryItems()
    }

    private fun observePantryItems() {
        viewModelScope.launch {
            pantryRepository.observePantryItems()
                .onEach { items ->
                    _uiState.value = PantryUiState(
                        isLoading = false,
                        items = items,
                        errorMessage = null
                    )
                }
                .catch { throwable ->
                    _uiState.value = PantryUiState(
                        isLoading = false,
                        items = emptyList(),
                        errorMessage = throwable.message ?: "Unable to load pantry items"
                    )
                }
                .collect {}
        }
    }
}
