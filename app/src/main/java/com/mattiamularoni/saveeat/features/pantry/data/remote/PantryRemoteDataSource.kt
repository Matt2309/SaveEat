package com.mattiamularoni.saveeat.features.pantry.data.remote

/**
 * Interface astratta per le operazioni remote su Supabase Postgrest.
 *
 * Responsabilità:
 * - Fetch/upload items da/verso pantry_items table
 * - Query specifiche (placeholder, expiring, search)
 */
interface PantryRemoteDataSource {
    /**
     * Recupera tutti gli elementi della dispensa dell'utente da Supabase.
     *
     * @param userId UUID dell'utente
     * @return lista di DTO dalla risposta Postgrest
     * @throws Exception in caso di errore rete o parsing
     */
    suspend fun getPantryItems(userId: String): List<PantryItemDto>

    /**
     * Aggiunge un nuovo elemento alla dispensa su Supabase.
     *
     * @param item DTO dell'elemento da inserire
     * @return DTO con ID generato dal backend
     * @throws Exception in caso di errore
     */
    suspend fun addPantryItem(item: PantryItemDto): PantryItemDto

    /**
     * Aggiorna un elemento esistente su Supabase.
     *
     * @param itemId UUID dell'elemento
     * @param updates mappa delle proprietà da aggiornare (solo campi necessari)
     * @return DTO aggiornato
     * @throws Exception in caso di errore
     */
    suspend fun updatePantryItem(itemId: String, updates: Map<String, Any?>): PantryItemDto

    /**
     * Cancella un elemento da Supabase.
     *
     * @param itemId UUID dell'elemento da cancellare
     * @return true se successo, false altrimenti
     * @throws Exception in caso di errore
     */
    suspend fun deletePantryItem(itemId: String): Boolean

    /**
     * Recupera i placeholder di un utente per il matching con prodotti reali.
     *
     * @param userId UUID dell'utente
     * @return lista di DTO dei placeholder
     * @throws Exception in caso di errore
     */
    suspend fun getPlaceholders(userId: String): List<PantryItemDto>

    /**
     * Cerca placeholder per nome (pattern matching).
     *
     * Utile per suggerimenti durante la creazione di placeholder o il matching.
     *
     * @param userId UUID dell'utente
     * @param query stringa di ricerca
     * @return lista di DTO matching
     * @throws Exception in caso di errore
     */
    suspend fun searchPlaceholders(userId: String, query: String): List<PantryItemDto>

    /**
     * Recupera gli elementi in scadenza entro una soglia di giorni.
     *
     * @param userId UUID dell'utente
     * @param thresholdMs timestamp in millisecondi (deadline per scadenza)
     * @return lista di DTO degli elementi in scadenza
     * @throws Exception in caso di errore
     */
    suspend fun getExpiringItems(userId: String, thresholdMs: Long): List<PantryItemDto>

    /**
     * Aggiunge items estratti da uno scontrino alla dispensa.
     *
     * Batch insert per effi cienza: carica multipli item in una sola transazione.
     *
     * @param receiptId UUID dello scontrino
     * @param items lista di DTO da inserire (con receiptId associato)
     * @return lista di DTO inseriti con ID generati
     * @throws Exception in caso di errore
     */
    suspend fun saveReceiptItems(receiptId: String, items: List<PantryItemDto>): List<PantryItemDto>
}
