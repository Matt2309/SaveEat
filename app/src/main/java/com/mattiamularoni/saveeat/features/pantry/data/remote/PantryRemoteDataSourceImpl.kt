package com.mattiamularoni.saveeat.features.pantry.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementazione di PantryRemoteDataSource usando Supabase Postgrest.
 *
 * Responsabilità:
 * - Eseguire query su Postgrest
 * - Mappare risposta JSON in DTO
 * - Gestire errori di rete e parsing
 * - Operare su Dispatchers.IO (non bloccare Main Thread)
 *
 * NOTE: MVP implementation - Postgrest query builder da integrare quando plugin disponibile.
 * Attualmente placeholder per testing dello stack.
 */
class PantryRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient
) : PantryRemoteDataSource {

    /**
     * Recupera tutti gli elementi della dispensa dell'utente da Supabase.
     *
     * Query Postgrest: SELECT * FROM pantry_items WHERE user_id = {userId}
     *
     * @param userId UUID dell'utente
     * @return lista di DTO
     * @throws Exception se parsing JSON fallisce o errore rete
     */
    override suspend fun getPantryItems(userId: String): List<PantryItemDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("pantry_items")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<PantryItemDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch pantry items: ${e.message}", e)
            }
        }

    /**
     * Aggiunge un nuovo elemento alla dispensa su Supabase.
     *
     * @param item DTO con almeno (name, category, user_id)
     * @return DTO con ID generato dal backend
     */
    override suspend fun addPantryItem(item: PantryItemDto): PantryItemDto =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("pantry_items")
                    .insert(item)
                    .decodeSingle<PantryItemDto>()
            } catch (e: Exception) {
                throw Exception("Failed to add pantry item: ${e.message}", e)
            }
        }

    /**
     * Aggiorna un elemento esistente su Supabase.
     *
     * @param itemId UUID dell'elemento
     * @param updates mappa delle proprietà da aggiornare
     * @return DTO aggiornato
     */
    override suspend fun updatePantryItem(
        itemId: String,
        updates: Map<String, Any>
    ): PantryItemDto =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("pantry_items")
                    .update(updates) {
                        filter {
                            eq("id", itemId)
                        }
                        select()
                    }
                    .decodeSingle<PantryItemDto>()
            } catch (e: Exception) {
                throw Exception("Failed to update pantry item: ${e.message}", e)
            }
        }

    /**
     * Cancella un elemento da Supabase.
     *
     * @param itemId UUID dell'elemento da cancellare
     * @return true se successo
     */
    override suspend fun deletePantryItem(itemId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("pantry_items")
                    .delete {
                        filter {
                            eq("id", itemId)
                        }
                    }
                true
            } catch (e: Exception) {
                throw Exception("Failed to delete pantry item: ${e.message}", e)
            }
        }

    /**
     * Recupera i placeholder di un utente.
     *
     * @param userId UUID dell'utente
     * @return lista di DTO placeholder
     */
    override suspend fun getPlaceholders(userId: String): List<PantryItemDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("pantry_items")
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("is_placeholder", true)
                        }
                    }
                    .decodeList<PantryItemDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch placeholders: ${e.message}", e)
            }
        }

    /**
     * Cerca placeholder per nome pattern.
     *
     * @param userId UUID dell'utente
     * @param query stringa di ricerca
     * @return lista di DTO matching
     */
    override suspend fun searchPlaceholders(userId: String, query: String): List<PantryItemDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("pantry_items")
                    .select {
                        filter {
                            eq("user_id", userId)
                            ilike("name", "%$query%")
                        }
                    }
                    .decodeList<PantryItemDto>()
            } catch (e: Exception) {
                throw Exception("Failed to search placeholders: ${e.message}", e)
            }
        }

    /**
     * Recupera gli elementi in scadenza entro una soglia temporale.
     *
     * @param userId UUID dell'utente
     * @param thresholdMs timestamp limite (ms)
     * @return lista di DTO in scadenza
     */
    override suspend fun getExpiringItems(userId: String, thresholdMs: Long): List<PantryItemDto> =
        withContext(Dispatchers.IO) {
            try {
                val thresholdInstant = java.time.Instant.ofEpochMilli(thresholdMs).toString()
                supabaseClient
                    .from("pantry_items")
                    .select {
                        filter {
                            eq("user_id", userId)
                            lte("expiration_date", thresholdInstant)
                        }
                    }
                    .decodeList<PantryItemDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch expiring items: ${e.message}", e)
            }
        }

    /**
     * Aggiunge items estratti da uno scontrino.
     *
     * Batch insert in una transazione per efficienza.
     *
     * @param receiptId UUID dello scontrino
     * @param items lista di DTO (con receiptId già impostato)
     * @return lista di DTO inseriti
     */
    override suspend fun saveReceiptItems(
        receiptId: String,
        items: List<PantryItemDto>
    ): List<PantryItemDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("pantry_items")
                    .insert(items)
                    .decodeList<PantryItemDto>()
            } catch (e: Exception) {
                throw Exception("Failed to save receipt items: ${e.message}", e)
            }
        }
}

