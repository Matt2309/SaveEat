package com.mattiamularoni.saveeat.features.stats.data.remote

/**
 * Interface astratta per le operazioni remote su Supabase Postgrest, tabella user_stats.
 */
interface UserStatsRemoteDataSource {
    /**
     * Recupera la riga di statistiche dell'utente da Supabase.
     *
     * @param userId UUID dell'utente
     * @return DTO se esiste una riga, null altrimenti
     * @throws Exception in caso di errore rete o parsing
     */
    suspend fun getUserStats(userId: String): UserStatsDto?

    /**
     * Crea o aggiorna (upsert) la riga di statistiche dell'utente su Supabase.
     *
     * @param dto DTO con i totali aggiornati
     * @return DTO salvato dal backend
     * @throws Exception in caso di errore
     */
    suspend fun upsertUserStats(dto: UserStatsDto): UserStatsDto
}
