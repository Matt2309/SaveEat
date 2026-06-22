package com.mattiamularoni.saveeat.features.home.data.remote

/**
 * Interface astratta per le operazioni remote su Supabase Postgrest.
 *
 * Responsabilità:
 * - Fetch dati aggregati dashboard (pantry, leaderboard, recipes)
 * - Query specifiche per componenti dashboard (expiring items, top users, etc.)
 * - Composizione dati da più sorgenti
 */
interface HomeRemoteDataSource {
    /**
     * Recupera i dati completi aggregati della dashboard Home.
     *
     * Combina:
     * - Item in scadenza dalla pantry (7 giorni)
     * - Top 3 utenti leaderboard
     * - Ricette suggerite matched con expiring ingredients
     * - Statistiche pantry (totale, expiring, placeholders)
     * - Profilo utente (name, avatar, rank)
     *
     * @param userId UUID dell'utente
     * @return DTO con dati aggregati dashboard
     * @throws Exception in caso di errore rete o parsing
     */
    suspend fun getHomeDashboard(userId: String): HomeDashboardDto

    /**
     * Recupera gli item in scadenza nei prossimi 7 giorni.
     *
     * Query: SELECT * FROM pantry_items WHERE user_id = ? AND expiration_date <= (now + 7 days)
     * ORDER BY expiration_date ASC
     *
     * @param userId UUID dell'utente
     * @return lista di ExpiringItemDto
     * @throws Exception in caso di errore
     */
    suspend fun getExpiringItems(userId: String): List<ExpiringItemDto>

    /**
     * Recupera le statistiche aggregate della pantry.
     *
     * Calcola:
     * - total_items: COUNT(*) FROM pantry_items WHERE user_id = ? AND status = 'active'
     * - expiring_count: COUNT(*) WHERE expiration_date <= (now + 7 days)
     * - active_placeholders: COUNT(*) WHERE is_placeholder = true
     * - eco_points: FROM leaderboard (vista users LEFT JOIN user_stats) WHERE id = ?
     *
     * @param userId UUID dell'utente
     * @return UserStatsDto con statistiche
     * @throws Exception in caso di errore
     */
    suspend fun getUserStats(userId: String): UserStatsDto

    /**
     * Recupera i top N utenti per eco_points (leaderboard ranking).
     *
     * Query: SELECT * FROM leaderboard ORDER BY eco_points DESC LIMIT 3
     *
     * @param limit numero di utenti da recuperare (default 3)
     * @return lista di LeaderboardUserDto ordinati DESC by eco_points
     * @throws Exception in caso di errore
     */
    suspend fun getTopLeaderboardUsers(limit: Int = 3): List<LeaderboardUserDto>

    /**
     * Recupera la posizione di ranking dell'utente corrente.
     *
     * Calcola: ROW_NUMBER() OVER (ORDER BY eco_points DESC) per l'utente
     *
     * @param userId UUID dell'utente
     * @return posizione nel ranking (1-indexed)
     * @throws Exception in caso di errore
     */
    suspend fun getUserRankPosition(userId: String): Int

    /**
     * Recupera tutte le ricette con ingredienti.
     *
     * Query: SELECT * FROM recipes (per matching locale)
     *
     * @return lista di SuggestedRecipeDto con metadati
     * @throws Exception in caso di errore
     */
    suspend fun getAllRecipes(): List<SuggestedRecipeDto>

    /**
     * Recupera i dati profilo dell'utente.
     *
     * Query: SELECT id, display_name, avatar_url FROM users WHERE id = ?
     *
     * @param userId UUID dell'utente
     * @return UserProfileDto
     * @throws Exception in caso di errore
     */
    suspend fun getUserProfile(userId: String): UserProfileDto
}
