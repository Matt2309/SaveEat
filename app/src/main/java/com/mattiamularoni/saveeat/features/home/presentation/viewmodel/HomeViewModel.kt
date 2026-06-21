package com.mattiamularoni.saveeat.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.home.presentation.domain.GetHomeDashboardUseCase
import com.mattiamularoni.saveeat.features.home.presentation.state.HomeUiState
import com.mattiamularoni.saveeat.features.stats.domain.model.UserStats
import com.mattiamularoni.saveeat.features.stats.domain.usecase.GetUserStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel per la home dashboard.
 *
 * Responsabilità:
 * - Gestire lo stato della dashboard (StateFlow<HomeUiState>)
 * - Coordinare refresh manuale (pull-to-refresh)
 * - Gestire caricamento, errori, e fallback offline
 * - Lifecycle management tramite viewModelScope
 *
 * Pattern:
 * - StateFlow per stato (Loading, Success, Error, Empty)
 * - viewModelScope per coroutine lifecycle-aware
 * - UseCase per aggregazione logica business
 *
 */
class HomeViewModel(
    private val getHomeDashboardUseCase: GetHomeDashboardUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val sessionProvider: SessionProvider
) : ViewModel() {

    val currentUserName = sessionProvider.getUserDisplayName()
    val currentUserId = sessionProvider.getCurrentUserId()

    // State: HomeUiState (Loading, Success, Error, Empty)
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Indica se il refresh è in corso
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Statistiche di risparmio (kg cibo salvato) per la SavedFoodCard
    val userStats: StateFlow<UserStats> = getUserStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserStats())

    init {
        // Subscribe al Flow della dashboard per aggiornamenti real-time
        subscribeToHomeDashboard()

        // Fetch fresh data from Supabase on every open; Room Flow will emit and update UI
        refreshDashboard()
    }

    /**
     * Sottoscrizione al Flow di dashboard per aggiornamenti real-time.
     *
     * Logica:
     * - Invoca use case per ottenere Flow<HomeDashboard>
     * - Map homeDashboard → Success state
     * - Cattura eccezioni → Error state
     * - Emette aggiornamenti nello StateFlow
     *
     * Subscription rimane attiva per la durata del ViewModel (viewModelScope).
     */
    private fun subscribeToHomeDashboard() {
        viewModelScope.launch {
            getHomeDashboardUseCase()
                .map { dashboard ->
                    try {
                        if (dashboard != null) HomeUiState.Success(dashboard) as HomeUiState
                        else HomeUiState.Empty
                    } catch (exception: Exception) {
                        HomeUiState.Error("Dashboard update failed: ${exception.message}")
                    }
                }
                .catch { exception ->
                    // Fallback: mantieni stato precedente oppure mostra errore
                    _uiState.value = HomeUiState.Error("Dashboard update failed: ${exception.message}")
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    /**
     * Refresh manuale della dashboard (pull-to-refresh).
     *
     * Logica:
     * 1. Set isRefreshing = true
     * 2. Invoca use case refresh
     * 3. Se successo: stato aggiornato via subscription (map Success)
     * 4. Se falso: stato rimane (fallback alla cache è okay per MVP)
     * 5. Set isRefreshing = false
     *
     * Operazione: coroutine async su Dispatchers.IO via use case
     */
    fun refreshDashboard() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true

                val success = getHomeDashboardUseCase.refresh()
                if (!success) {
                    // Refresh fallito, ma cache rimane valida
                    // Mostra toast/snackbar opzionale via event (future)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Refresh failed: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Cancella manualmente i dati dalla cache (logout, etc.).
     *
     * Operazione distruttiva: rimuove HomeDashboardEntity dal database.
     */
    fun clearDashboard() {
        viewModelScope.launch {
            try {
                // TODO: Implementare metodo deleteDashboard nel repository se necessario
                _uiState.value = HomeUiState.Empty
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Clear failed: ${e.message}")
            }
        }
    }
}
