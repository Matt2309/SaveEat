package com.mattiamularoni.saveeat.features.pantry.domain.repository

import kotlinx.coroutines.flow.Flow

interface PantryRepository {
    // ===== OBSERVE & SYNC =====

    /**
     * Osserva tutti gli elementi della dispensa dell'utente con aggiornamenti real-time.
     *
     * Logica:
     * - Fetch iniziale da Supabase
     * - Cache in Room
     * - Flow emette ogni cambio Room-local
     * - Background sync in progress
     *
     * @return Flow della lista pantry items aggiornato
     */
    fun observePantryItems(): Flow<List<PantryItem>>

    /**
     * Sincronizza la dispensa con Supabase.
     *
     * Logica:
     * - Fetch items da Supabase per l'utente
     * - Cache in Room (upsert)
     * - Ritorna numero items sincronizzati
     *
     * @return numero di item sincronizzati
     * @throws Exception in caso di errore rete
     */
    suspend fun syncPantry(): Int

    // ===== CRUD BASIC =====

    /**
     * Recupera un singolo item della dispensa per ID.
     *
     * Legge dalla cache Room locale.
     *
     * @param itemId UUID dell'elemento
     * @return PantryItem se trovato, null altrimenti
     */
    suspend fun getPantryItemById(itemId: String): PantryItem?

    /**
     * Aggiunge un nuovo elemento alla dispensa.
     *
     * Logica:
     * - Insert in Supabase remoto
     * - Cache in Room locale
     * - Ritorna ID generato dal server
     *
     * @param item elemento da aggiungere
     * @return UUID dell'elemento creato
     * @throws Exception in caso di errore
     */
    suspend fun addPantryItem(item: PantryItem): String

    /**
     * Aggiorna un elemento esistente della dispensa.
     *
     * Logica:
     * - Update su Supabase remoto
     * - Update in Room locale
     *
     * @param itemId UUID dell'elemento
     * @param item dati aggiornati
     * @return true se successo, false se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun updatePantryItem(itemId: String, item: PantryItem): Boolean

    /**
     * Cancella un elemento dalla dispensa.
     *
     * Logica:
     * - Delete su Supabase remoto
     * - Delete in Room locale
     *
     * @param itemId UUID dell'elemento
     * @return true se cancellato, false se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun deletePantryItem(itemId: String): Boolean

    // ===== PLACEHOLDER MANAGEMENT =====

    /**
     * Aggiunge un placeholder (elemento pianificato della spesa).
     *
     * Logica:
     * - Insert con isPlaceholder=true
     * - Nome e categoria forniti
     * - No expiration date (non necessaria per placeholder)
     *
     * @param name nome dell'elemento pianificato (es. "Latte")
     * @param category categoria (FRIDGE, PANTRY, FREEZER)
     * @return UUID del placeholder creato
     * @throws Exception in caso di errore
     */
    suspend fun addPlaceholder(name: String, category: String): String

    /**
     * Converte un placeholder in un elemento reale quando viene trovato
     * un match durante la scansione dello scontrino.
     *
     * Logica:
     * - Localizza placeholder per ID
     * - Aggiorna: isPlaceholder=false, receiptId set, metadata reale
     * - Preserva timestamp createdAt originale
     * - Aggiorna updatedAt al momento della conversione
     *
     * @param placeholderId UUID del placeholder
     * @param realItem elemento reale estratto dallo scontrino
     * @throws Exception se placeholder non trovato o errore update
     */
    suspend fun convertPlaceholderToRealItem(placeholderId: String, realItem: PantryItem)

    /**
     * Trova il miglior placeholder matching per un nome di prodotto.
     *
     * Logica:
     * - Fuzzy match su nome (PlaceholderMatcher)
     * - Calcola similarity score per tutti i placeholder
     * - Ritorna il top match se score > soglia (0.7)
     *
     * @param itemName nome dell'elemento reale estratto dallo scontrino
     * @return PantryItem placeholder matching, o null se nessun match
     */
    suspend fun findMatchingPlaceholder(itemName: String): PantryItem?

    /**
     * Aggiorna un placeholder (es. nome, categoria).
     *
     * @param placeholderId UUID del placeholder
     * @param updates mappa (fieldName → newValue) da aggiornare
     * @return true se successo, false se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun updatePlaceholder(placeholderId: String, updates: Map<String, Any>): Boolean

    /**
     * Rimuove un placeholder dalla dispensa.
     *
     * @param placeholderId UUID del placeholder
     * @return true se rimosso, false se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun removePlaceholder(placeholderId: String): Boolean

    // ===== RECEIPT INTEGRATION =====

    /**
     * Salva gli elementi estratti da uno scontrino nella dispensa.
     *
     * Logica:
     * - Items con receipt_id associato
     * - Batch insert su Supabase
     * - Cache in Room
     * - Tentativo di matching placeholder automatico
     *
     * @param receiptId UUID dello scontrino
     * @param items elementi estratti da Gemini OCR
     * @throws Exception in caso di errore
     */
    suspend fun saveReceiptItems(receiptId: String, items: List<PantryItem>)

    /**
     * Merge automatico di items estratti da scontrino con placeholder esistenti.
     *
     * Logica:
     * - Per ogni item da scontrino, cerca matching placeholder
     * - Converte placeholder → elemento reale se match trovato
     * - Crea nuovo elemento se nessun match
     *
     * Responsabilità: (internamente chiamata da saveReceiptItems)
     *
     * @param receiptId UUID dello scontrino
     * @param receiptItems items estratti dallo scontrino
     * @throws Exception in caso di errore
     */
    suspend fun mergeReceiptItemsWithPantry(receiptId: String, receiptItems: List<PantryItem>)

    /**
     * Deduplica gli elementi della dispensa.
     *
     * Logica:
     * - Identifica duplicati per (user_id, name, category)
     * - Mantiene l'elemento più recente (max updatedAt)
     * - Cancella duplicati
     *
     * Responsabilità: cleanup periodico per evitare accumulo
     *
     * @return numero di duplicati rimossi
     * @throws Exception in caso di errore
     */
    suspend fun deduplicatePantryItems(): Int

    // ===== STATUS & EXPIRATION =====

    /**
     * Aggiorna lo stato di un elemento (active, consumed, discarded, expired).
     *
     * @param itemId UUID dell'elemento
     * @param status nuovo stato
     * @return true se successo, false se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun updateItemStatus(itemId: String, status: String): Boolean

    /**
     * Aggiorna la data di scadenza di un elemento.
     *
     * @param itemId UUID dell'elemento
     * @param expirationDate timestamp in millisecondi (ms from epoch)
     * @return true se successo, false se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun updateExpirationDate(itemId: String, expirationDate: Long): Boolean

    /**
     * Osserva gli elementi in scadenza entro una soglia di giorni.
     *
     * Logica:
     * - Calcola timestamp threshold (oggi + N giorni)
     * - Filtra items con expiration_date <= threshold
     * - Emette Flow aggiornato real-time
     *
     * @param thresholdDays numero di giorni da oggi (es. 5 = scadono nei prossimi 5 giorni)
     * @return Flow della lista di items in scadenza
     */
    fun getExpiringItems(thresholdDays: Int): Flow<List<PantryItem>>
}

/**
 * Modello di dominio per gli elementi della dispensa.
 *
 * Rappresenta un singolo prodotto/placeholder con tutti i metadati.
 */
data class PantryItem(
    val id: String,
    val userId: String,
    val receiptId: String?,
    val name: String,
    val category: String,
    val categoryKey: String = "",
    val isPlaceholder: Boolean,
    val status: String,
    val quantity: Double,
    val unit: String?,
    val expirationDate: Long?
)
