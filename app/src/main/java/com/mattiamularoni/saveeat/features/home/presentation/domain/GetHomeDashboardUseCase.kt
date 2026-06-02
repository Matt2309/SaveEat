package com.mattiamularoni.saveeat.features.home.presentation.domain

import com.mattiamularoni.saveeat.features.home.domain.repository.HomeDashboard
import com.mattiamularoni.saveeat.features.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case per il recupero e osservazione della dashboard Home.
 *
 * Responsabilità:
 * - Orchestrare il repository per ottenere i dati dashboard
 * - Esporre Flow di HomeDashboard per la reattività UI
 * - Gestire il ciclo di vita (subscription, unsubscription)
 *
 * Implementato con il pattern operator fun invoke() per brevità.
 * NOTE: userId è gestito internamente nel repository (MVP: "test-user-uuid")
 */
class GetHomeDashboardUseCase(
    private val homeRepository: HomeRepository
) {

    /**
     * Osserva i dati della dashboard Home per l'utente corrente.
     *
     * Ritorna un Flow che emette HomeDashboard ogni volta che i dati cambiano.
     * La subscription rimane attiva fino a quando il Flow non viene cancellato.
     *
     * @return Flow<HomeDashboard> che emette dati aggregati dashboard
     */
    operator fun invoke(): Flow<HomeDashboard> {
        return homeRepository.observeHomeDashboard()
    }

    /**
     * Refresh manuale della dashboard da Supabase.
     *
     * Fetch aggregati i dati dashboard, cache in Room, e notifica via Flow.
     *
     * @return true se refresh riuscito, false se errore (ma fallback alla cache rimane)
     */
    suspend fun refresh(): Boolean {
        return homeRepository.refreshHomeDashboard()
    }

    /**
     * Recupera snapshot singolo della dashboard dalla cache.
     *
     * Operazione one-off (non Flow).
     *
     * @return HomeDashboard se disponibile in cache, null altrimenti
     */
    suspend fun getCachedDashboard(): HomeDashboard? {
        return homeRepository.getHomeDashboard()
    }
}
