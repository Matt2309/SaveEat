package com.mattiamularoni.saveeat.features.leaderboard.data.remote

/**
 * Interface astratta per operazioni remote su Supabase Postgrest.
 *
 * Responsabilità:
 * - Fetch leaderboard globale ordinata per eco_points (vista `leaderboard`: users LEFT JOIN user_stats)
 * - Fetch top N utenti
 * - Calcolo ranking utente
 *
 * eco_points è ora calcolato in user_stats.total_eco_points (unica fonte di verità);
 * la vista `leaderboard` lo espone con lo stesso nome di colonna per compatibilità con i DTO.
 */
interface LeaderboardRemoteDataSource {
    /**
     * Recupera la leaderboard globale ordinata per eco_points DESC.
     *
     * Query Postgrest: SELECT * FROM leaderboard ORDER BY eco_points DESC
     *
     * @return lista di DTO ordinata per punteggio decrescente
     * @throws Exception in caso di errore rete o parsing
     */
    suspend fun getLeaderboard(): List<LeaderboardUserDto>

    /**
     * Recupera i top N utenti della leaderboard.
     *
     * Query Postgrest: SELECT * FROM leaderboard ORDER BY eco_points DESC LIMIT :limit
     *
     * @param limit numero massimo di risultati
     * @return lista dei top N DTO
     * @throws Exception in caso di errore
     */
    suspend fun getTopUsers(limit: Int): List<LeaderboardUserDto>

    /**
     * Recupera la posizione di un utente specifico nella leaderboard.
     *
     * Query Postgrest: SELECT * FROM leaderboard WHERE id = :userId
     *
     * @param userId UUID dell'utente
     * @return DTO dell'utente, null se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun getUserById(userId: String): LeaderboardUserDto?
}
