package com.mattiamularoni.saveeat.features.leaderboard.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface per operazioni sulla leaderboard globale.
 *
 * Responsabilità:
 * - Orchestrare fetch dati Supabase + caching
 * - Esporre dati come Flow per streaming real-time
 * - Supportare refresh manuale
 * - Gestire ranking e aggiornamenti eco_points
 */
interface LeaderboardRepository {
    /**
     * Osserva la leaderboard globale con aggiornamenti real-time.
     *
     * Logica:
     * - Fetch iniziale da Supabase (ordinato per eco_points DESC)
     * - Calcola rank client-side basato su ordinamento
     * - Flow emette aggiornamenti
     *
     * @return Flow della lista leaderboard utenti ordinata per punteggio decrescente
     */
    fun observeLeaderboard(): Flow<List<LeaderboardUser>>

    /**
     * Sincronizza la leaderboard con Supabase.
     *
     * Operazione: Dispatchers.IO (remote fetch)
     *
     * @return numero di utenti sincronizzati
     * @throws Exception in caso di errore rete o parsing
     */
    suspend fun refreshLeaderboard(): Int

    /**
     * Recupera la posizione dell'utente corrente nella leaderboard.
     *
     * Logica:
     * - Fetch leaderboard ordinata
     * - Ricerca utente per ID
     * - Calcola rank posizionale
     *
     * @param userId UUID dell'utente
     * @return LeaderboardUser con rank valorizzato, null se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun getCurrentUserRank(userId: String): LeaderboardUser?

    /**
     * Recupera i top N utenti della classifica.
     *
     * @param limit numero massimo di utenti da recuperare (default 10)
     * @return lista dei top utenti ordinata per eco_points DESC
     * @throws Exception in caso di errore
     */
    suspend fun getTopUsers(limit: Int = 10): List<LeaderboardUser>
}
