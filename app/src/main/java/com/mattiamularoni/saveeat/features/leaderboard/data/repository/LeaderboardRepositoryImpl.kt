package com.mattiamularoni.saveeat.features.leaderboard.data.repository

import com.mattiamularoni.saveeat.features.leaderboard.data.mapper.LeaderboardMapper
import com.mattiamularoni.saveeat.features.leaderboard.data.remote.LeaderboardRemoteDataSource
import com.mattiamularoni.saveeat.features.leaderboard.domain.repository.LeaderboardRepository
import com.mattiamularoni.saveeat.features.leaderboard.domain.repository.LeaderboardUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Implementazione di LeaderboardRepository con fetch Supabase.
 *
 * Responsabilità:
 * - Orchestrare remote datasource
 * - Logica business (ranking, ordinamento)
 * - Operare su Dispatchers.IO (async, no Main Thread blocking)
 * - Esporre dati come Flow per streaming
 */
class LeaderboardRepositoryImpl(
    private val remoteDataSource: LeaderboardRemoteDataSource
) : LeaderboardRepository {

    /**
     * Osserva la leaderboard globale con aggiornamenti real-time.
     *
     * Logica:
     * - Fetch iniziale da Supabase
     * - Calcola rank client-side basato su ordinamento
     * - Flow emette lista con rank valorizzato
     *
     * @return Flow della lista leaderboard aggiornato
     */
    override fun observeLeaderboard(): Flow<List<LeaderboardUser>> = flow {
        try {
            val users = refreshLeaderboardInternal()
            emit(users)
        } catch (e: Exception) {
            throw Exception("Failed to observe leaderboard: ${e.message}", e)
        }
    }

    /**
     * Sincronizza la leaderboard con Supabase.
     *
     * Operazione: Dispatchers.IO (remote fetch)
     *
     * @return numero di utenti sincronizzati
     * @throws Exception in caso di errore rete o parsing
     */
    override suspend fun refreshLeaderboard(): Int = withContext(Dispatchers.IO) {
        try {
            val users = refreshLeaderboardInternal()
            users.size
        } catch (e: Exception) {
            throw Exception("Sync failed: ${e.message}", e)
        }
    }

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
    override suspend fun getCurrentUserRank(userId: String): LeaderboardUser? =
        withContext(Dispatchers.IO) {
            try {
                val leaderboard = refreshLeaderboardInternal()
                leaderboard.firstOrNull { it.id == userId }
            } catch (e: Exception) {
                throw Exception("Failed to get user rank: ${e.message}", e)
            }
        }

    /**
     * Recupera i top N utenti della classifica.
     *
     * @param limit numero massimo di utenti da recuperare (default 10)
     * @return lista dei top utenti ordinata per eco_points DESC
     * @throws Exception in caso di errore
     */
    override suspend fun getTopUsers(limit: Int): List<LeaderboardUser> =
        withContext(Dispatchers.IO) {
            try {
                val dtos = remoteDataSource.getTopUsers(limit)
                LeaderboardMapper.dtosToDomain(dtos)
                    .mapIndexed { index, user -> user.copy(rank = index + 1) }
            } catch (e: Exception) {
                throw Exception("Failed to fetch top users: ${e.message}", e)
            }
        }

    // ===== PRIVATE HELPERS =====

    /**
     * Logica interna di sincronizzazione con ranking client-side.
     *
     * @return lista leaderboard con rank valorizzato
     */
    private suspend fun refreshLeaderboardInternal(): List<LeaderboardUser> =
        withContext(Dispatchers.IO) {
            val dtos = remoteDataSource.getLeaderboard()
            LeaderboardMapper.dtosToDomain(dtos)
                .mapIndexed { index, user -> user.copy(rank = index + 1) }
        }
}
