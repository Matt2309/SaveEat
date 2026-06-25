package com.mattiamularoni.saveeat.features.leaderboard.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementazione di LeaderboardRemoteDataSource usando Supabase Postgrest.
 *
 * Responsabilità:
 * - Eseguire query su Postgrest per users table
 * - Mappare risposta JSON in DTO
 * - Gestire errori di rete e parsing
 * - Operare su Dispatchers.IO (non bloccare Main Thread)
 *
 * NOTE: MVP implementation - Postgrest query builder da integrare quando plugin disponibile.
 * Attualmente placeholder per testing dello stack.
 */
class LeaderboardRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient,
) : LeaderboardRemoteDataSource {
    /**
     * Recupera la leaderboard globale ordinata per eco_points DESC da Supabase.
     *
     * Query Postgrest: SELECT * FROM leaderboard ORDER BY eco_points DESC
     *
     * Gli eco_points sono ora calcolati dalla vista `leaderboard` (users LEFT JOIN user_stats),
     * unica fonte di verità: user_stats.total_eco_points.
     *
     * @return lista di DTO ordinata per punteggio decrescente
     * @throws Exception in caso di errore rete o parsing
     */
    override suspend fun getLeaderboard(): List<LeaderboardUserDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("leaderboard")
                    .select {
                        order("eco_points", Order.DESCENDING)
                    }.decodeList<LeaderboardUserDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch leaderboard: ${e.message}", e)
            }
        }

    /**
     * Recupera i top N utenti della leaderboard da Supabase.
     *
     * Query Postgrest: SELECT * FROM leaderboard ORDER BY eco_points DESC LIMIT :limit
     *
     * @param limit numero massimo di risultati
     * @return lista dei top N DTO
     * @throws Exception in caso di errore
     */
    override suspend fun getTopUsers(limit: Int): List<LeaderboardUserDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("leaderboard")
                    .select {
                        order("eco_points", Order.DESCENDING)
                        limit(limit.toLong())
                    }.decodeList<LeaderboardUserDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch top users: ${e.message}", e)
            }
        }

    /**
     * Recupera la posizione di un utente specifico nella leaderboard.
     *
     * Query Postgrest: SELECT * FROM leaderboard WHERE id = :userId
     *
     * @param userId UUID dell'utente
     * @return DTO dell'utente, null se non trovato
     * @throws Exception in caso di errore
     */
    override suspend fun getUserById(userId: String): LeaderboardUserDto? =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("leaderboard")
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }.decodeList<LeaderboardUserDto>()
                    .firstOrNull()
            } catch (e: Exception) {
                throw Exception("Failed to fetch user: ${e.message}", e)
            }
        }
}
