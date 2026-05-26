package com.mattiamularoni.saveeat.features.leaderboard.data.remote

/**
 * Interface astratta per operazioni remote su Supabase Postgrest.
 *
 * Responsabilità:
 * - Fetch leaderboard globale ordinata per eco_points
 * - Fetch top N utenti
 * - Calcolo ranking utente
 * - Update eco_points
 */
interface LeaderboardRemoteDataSource {

    /**
     * Recupera la leaderboard globale ordinata per eco_points DESC.
     *
     * Query Postgrest: SELECT * FROM users ORDER BY eco_points DESC
     *
     * @return lista di DTO ordinata per punteggio decrescente
     * @throws Exception in caso di errore rete o parsing
     */
    suspend fun getLeaderboard(): List<LeaderboardUserDto>

    /**
     * Recupera i top N utenti della leaderboard.
     *
     * Query Postgrest: SELECT * FROM users ORDER BY eco_points DESC LIMIT :limit
     *
     * @param limit numero massimo di risultati
     * @return lista dei top N DTO
     * @throws Exception in caso di errore
     */
    suspend fun getTopUsers(limit: Int): List<LeaderboardUserDto>

    /**
     * Recupera la posizione di un utente specifico nella leaderboard.
     *
     * Query Postgrest: SELECT * FROM users WHERE id = :userId
     *
     * @param userId UUID dell'utente
     * @return DTO dell'utente, null se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun getUserById(userId: String): LeaderboardUserDto?

    /**
     * Aggiorna gli eco_points di un utente.
     *
     * Query Postgrest: UPDATE users SET eco_points = eco_points + :points WHERE id = :userId
     *
     * @param userId UUID dell'utente
     * @param points numero di punti da aggiungere (negativo per sottrarre)
     * @return nuovo totale eco_points
     * @throws Exception in caso di errore
     */
    suspend fun updateEcoPoints(userId: String, points: Int): Int
}
