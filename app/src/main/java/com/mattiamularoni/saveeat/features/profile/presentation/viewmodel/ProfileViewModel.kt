package com.mattiamularoni.saveeat.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.features.auth.domain.usecase.SignOutUseCase
import com.mattiamularoni.saveeat.features.home.presentation.domain.GetHomeDashboardUseCase
import com.mattiamularoni.saveeat.features.stats.domain.usecase.GetUserStatsUseCase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Stato UI della schermata Profilo.
 */
data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val ecoPoints: Int = 0,
    val savedEuros: Double = 0.0
)

/**
 * ViewModel della schermata Profilo.
 *
 * Fonti dati:
 * - nome  -> SessionProvider (metadati utente Supabase)
 * - email -> Supabase Auth (currentUser)
 * - eco-punti / avatar -> dashboard in cache (GetHomeDashboardUseCase)
 * - euro risparmiati -> statistiche utente (GetUserStatsUseCase)
 * - logout -> SignOutUseCase
 */
class ProfileViewModel(
    private val sessionProvider: SessionProvider,
    private val supabaseClient: SupabaseClient,
    private val getHomeDashboardUseCase: GetHomeDashboardUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        val name = sessionProvider.getUserDisplayName()
        val email = supabaseClient.auth.currentUserOrNull()?.email.orEmpty()
        _state.update { it.copy(name = name, email = email) }

        viewModelScope.launch {
            val dashboard = runCatching { getHomeDashboardUseCase.getCachedDashboard() }.getOrNull()
            if (dashboard != null) {
                _state.update {
                    it.copy(
                        ecoPoints = dashboard.userStats.ecoPoints,
                        avatarUrl = dashboard.userProfile.avatarUrl ?: it.avatarUrl
                    )
                }
            }
        }

        viewModelScope.launch {
            getUserStatsUseCase().collect { stats ->
                _state.update { it.copy(savedEuros = stats.totalEurosSaved) }
            }
        }
    }

    /**
     * Esegue il logout. La navigazione verso il Login è gestita automaticamente
     * dall'effetto su sessionStatus nel NavHost (la sessione diventa NotAuthenticated).
     */
    fun signOut() {
        viewModelScope.launch {
            runCatching { signOutUseCase() }
        }
    }
}
