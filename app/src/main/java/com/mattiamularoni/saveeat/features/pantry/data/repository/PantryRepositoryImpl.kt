package com.mattiamularoni.saveeat.features.pantry.data.repository

import com.mattiamularoni.saveeat.core.data.remote.SessionProvider
import com.mattiamularoni.saveeat.core.util.ingredientMatchesPantryName
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryDao
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryEntity
import com.mattiamularoni.saveeat.features.pantry.data.mapper.PantryMapper
import com.mattiamularoni.saveeat.features.pantry.data.mapper.PlaceholderMatcher
import com.mattiamularoni.saveeat.features.pantry.data.remote.PantryRemoteDataSource
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryItem
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Implementazione di PantryRepository con sincronizzazione Supabase e caching Room.
 *
 * Responsabilità:
 * - Orchestrare remote datasource + local cache
 * - Logica business (placeholder matching, deduplica, receipt integration)
 * - Offline-first pattern: fetch remoto → cache locale → query su Flow
 * - Gestione errori e retry
 * - Operare su Dispatchers.IO (async, no Main Thread blocking)
 */
class PantryRepositoryImpl(
    private val pantryDao: PantryDao,
    private val remoteDataSource: PantryRemoteDataSource,
    private val sessionProvider: SessionProvider,
) : PantryRepository {
    // ===== OBSERVE & SYNC =====

    /**
     * Osserva tutti gli elementi della dispensa con aggiornamenti real-time.
     *
     * Logica:
     * - Fetch iniziale da Supabase
     * - Cache in Room (upsert)
     * - Flow primario da Room (aggiornamenti real-time)
     * - Side effect: background sync automatico
     *
     * @return Flow della lista pantry items aggiornato
     */
    override fun observePantryItems(): Flow<List<PantryItem>> =
        pantryDao
            .getPantryItems(sessionProvider.getCurrentUserId())
            .map { entities -> entities.map { entity -> entityToDomain(entity) } }

    /**
     * Sincronizza la dispensa con Supabase.
     *
     * Logica:
     * - Fetch items da Supabase per l'utente
     * - Cache in Room (upsert con REPLACE strategy)
     * - Ritorna conteggio items sincronizzati
     *
     * Operazione: Dispatchers.IO (remote call)
     *
     * @return numero di item sincronizzati
     * @throws Exception in caso di errore rete o parsing
     */
    override suspend fun syncPantry(): Int =
        withContext(Dispatchers.IO) {
            try {
                val remoteItems = remoteDataSource.getPantryItems(sessionProvider.getCurrentUserId())
                val entities = PantryMapper.dtosToEntities(remoteItems)
                pantryDao.insertPantryItems(entities)
                entities.size
            } catch (e: Exception) {
                throw Exception("Sync failed: ${e.message}", e)
            }
        }

    // ===== CRUD BASIC =====

    /**
     * Recupera un singolo item della dispensa per ID.
     *
     * Legge dalla cache Room locale (query sincrona).
     *
     * @param itemId UUID dell'elemento
     * @return PantryItem se trovato, null altrimenti
     */
    override suspend fun getPantryItemById(itemId: String): PantryItem? =
        withContext(Dispatchers.IO) {
            pantryDao.getPantryItemById(itemId)?.let { entity ->
                entityToDomain(entity)
            }
        }

    /**
     * Aggiunge un nuovo elemento alla dispensa.
     *
     * Logica:
     * - Genera UUID locale
     * - Insert su Supabase remoto
     * - Cache in Room locale
     * - Ritorna ID generato dal server (o quello locale se server non assegna)
     *
     * @param item elemento da aggiungere (id ignorato, generato nel metodo)
     * @return UUID dell'elemento creato
     * @throws Exception in caso di errore rete o database
     */
    override suspend fun addPantryItem(item: PantryItem): String =
        withContext(Dispatchers.IO) {
            try {
                val newId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()

                val entity =
                    PantryEntity(
                        id = newId,
                        userId = sessionProvider.getCurrentUserId(),
                        receiptId = item.receiptId,
                        name = item.name,
                        category = item.category,
                        categoryKey = item.categoryKey,
                        isPlaceholder = item.isPlaceholder,
                        status = item.status,
                        quantity = item.quantity,
                        unit = item.unit,
                        expirationDate = item.expirationDate,
                        createdAt = now,
                        updatedAt = now,
                    )

                val dto = PantryMapper.entityToDto(entity)
                val remoteResponse = remoteDataSource.addPantryItem(dto)

                // Cache response dal server (potrebbe avere ID diverso)
                val cachedEntity = PantryMapper.dtoToEntity(remoteResponse)
                pantryDao.insertPantryItem(cachedEntity)

                remoteResponse.id
            } catch (e: Exception) {
                throw Exception("Failed to add pantry item: ${e.message}", e)
            }
        }

    /**
     * Aggiorna un elemento esistente della dispensa.
     *
     * Logica:
     * - Localizza item in Room
     * - Merge con dati aggiornati
     * - Update su Supabase remoto
     * - Update in Room locale
     * - Aggiorna updatedAt timestamp
     *
     * @param itemId UUID dell'elemento
     * @param item dati aggiornati
     * @return true se successo, false se non trovato
     * @throws Exception in caso di errore
     */
    override suspend fun updatePantryItem(
        itemId: String,
        item: PantryItem,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val existing = pantryDao.getPantryItemById(itemId) ?: return@withContext false

                val updateMap: Map<String, Any?> =
                    mapOf(
                        "name" to item.name,
                        "category" to item.category,
                        "quantity" to item.quantity,
                        "unit" to (item.unit ?: ""),
                        "expiration_date" to
                            item.expirationDate?.let {
                                Instant.ofEpochMilli(it).toString()
                            },
                        "status" to item.status,
                        "updated_at" to Instant.now().toString(),
                    )

                remoteDataSource.updatePantryItem(itemId, updateMap)

                val updatedEntity =
                    existing.copy(
                        name = item.name,
                        category = item.category,
                        quantity = item.quantity,
                        unit = item.unit,
                        expirationDate = item.expirationDate,
                        status = item.status,
                        updatedAt = System.currentTimeMillis(),
                    )

                pantryDao.updatePantryItem(updatedEntity)
                true
            } catch (e: Exception) {
                throw Exception("Failed to update pantry item: ${e.message}", e)
            }
        }

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
    override suspend fun deletePantryItem(itemId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val existed = pantryDao.getPantryItemById(itemId) != null

                if (existed) {
                    remoteDataSource.deletePantryItem(itemId)
                    pantryDao.deletePantryItemById(itemId)
                }

                existed
            } catch (e: Exception) {
                throw Exception("Failed to delete pantry item: ${e.message}", e)
            }
        }

    // ===== PLACEHOLDER MANAGEMENT =====

    /**
     * Aggiunge un placeholder (elemento pianificato della spesa).
     *
     * Logica:
     * - Genera UUID
     * - Insert con isPlaceholder=true
     * - No expiration date (non necessaria)
     * - Status = "active"
     *
     * @param name nome dell'elemento pianificato (es. "Latte")
     * @param category categoria (FRIDGE, PANTRY, FREEZER)
     * @return UUID del placeholder creato
     * @throws Exception in caso di errore
     */
    override suspend fun addPlaceholder(
        name: String,
        category: String,
    ): String =
        withContext(Dispatchers.IO) {
            try {
                val placeholderId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()

                val entity =
                    PantryEntity(
                        id = placeholderId,
                        userId = sessionProvider.getCurrentUserId(),
                        receiptId = null,
                        name = name,
                        category = category,
                        isPlaceholder = true,
                        status = "ACTIVE",
                        quantity = 1.0,
                        unit = null,
                        expirationDate = null,
                        createdAt = now,
                        updatedAt = now,
                    )

                val dto = PantryMapper.entityToDto(entity)
                val remoteResponse = remoteDataSource.addPantryItem(dto)
                val cachedEntity = PantryMapper.dtoToEntity(remoteResponse)
                pantryDao.insertPantryItem(cachedEntity)

                remoteResponse.id
            } catch (e: Exception) {
                throw Exception("Failed to add placeholder: ${e.message}", e)
            }
        }

    /**
     * Converte un placeholder in un elemento reale quando viene trovato
     * un match durante la scansione dello scontrino.
     *
     * Logica:
     * - Localizza placeholder per ID
     * - Aggiorna: isPlaceholder=false, receiptId set, metadata reale
     * - Preserva createdAt originale
     * - Aggiorna updatedAt al momento della conversione
     * - Update su Supabase e Room
     *
     * @param placeholderId UUID del placeholder
     * @param realItem elemento reale estratto dallo scontrino
     * @throws Exception se placeholder non trovato o errore update
     */
    override suspend fun convertPlaceholderToRealItem(
        placeholderId: String,
        realItem: PantryItem,
    ): Unit =
        withContext(Dispatchers.IO) {
            try {
                val placeholder =
                    pantryDao.getPantryItemById(placeholderId)
                        ?: throw Exception("Placeholder not found: $placeholderId")

                val updateMap: Map<String, Any?> =
                    mapOf(
                        "is_placeholder" to false,
                        "receipt_id" to (realItem.receiptId ?: ""),
                        "name" to realItem.name,
                        "category" to realItem.category,
                        "quantity" to realItem.quantity,
                        "unit" to (realItem.unit ?: ""),
                        "expiration_date" to
                            realItem.expirationDate?.let {
                                Instant.ofEpochMilli(it).toString()
                            },
                        "status" to "ACTIVE",
                        "updated_at" to Instant.now().toString(),
                    )

                remoteDataSource.updatePantryItem(placeholderId, updateMap)

                val convertedEntity =
                    placeholder.copy(
                        isPlaceholder = false,
                        receiptId = realItem.receiptId,
                        name = realItem.name,
                        category = realItem.category,
                        categoryKey = realItem.categoryKey,
                        quantity = realItem.quantity,
                        unit = realItem.unit,
                        expirationDate = realItem.expirationDate,
                        status = "ACTIVE",
                        updatedAt = System.currentTimeMillis(),
                    )

                pantryDao.updatePantryItem(convertedEntity)
            } catch (e: Exception) {
                throw Exception("Failed to convert placeholder: ${e.message}", e)
            }
        }

    /**
     * Trova il miglior placeholder matching per un nome di prodotto.
     *
     * Logica:
     * - Recupera tutti i placeholder dell'utente
     * - Fuzzy match su nome (PlaceholderMatcher.fuzzyMatch)
     * - Calcola similarity score per tutti
     * - Ritorna il top match se score > 0.7
     *
     * @param itemName nome dell'elemento reale estratto dallo scontrino
     * @return PantryItem placeholder matching, o null se nessun match
     */
    override suspend fun findMatchingPlaceholder(itemName: String): PantryItem? =
        withContext(Dispatchers.IO) {
            try {
                val placeholders = pantryDao.getPlaceholders(sessionProvider.getCurrentUserId())

                val bestMatch =
                    placeholders
                        .filter { placeholder ->
                            PlaceholderMatcher.fuzzyMatch(placeholder.name, itemName)
                        }.maxByOrNull { placeholder ->
                            PlaceholderMatcher.similarityScore(placeholder.name, itemName)
                        }

                val score =
                    bestMatch?.let {
                        PlaceholderMatcher.similarityScore(it.name, itemName)
                    } ?: 0.0

                if (bestMatch != null && score > 0.7) {
                    entityToDomain(bestMatch)
                } else {
                    null
                }
            } catch (e: Exception) {
                throw Exception("Failed to find matching placeholder: ${e.message}", e)
            }
        }

    /**
     * Aggiorna un placeholder (es. nome, categoria).
     *
     * @param placeholderId UUID del placeholder
     * @param updates mappa (fieldName → newValue) da aggiornare
     * @return true se successo, false se non trovato
     * @throws Exception in caso di errore
     */
    override suspend fun updatePlaceholder(
        placeholderId: String,
        updates: Map<String, Any>,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val existing = pantryDao.getPantryItemById(placeholderId) ?: return@withContext false

                if (!existing.isPlaceholder) {
                    throw Exception("Item is not a placeholder")
                }

                val updateMap: Map<String, Any> =
                    updates.toMutableMap().apply {
                        this["updated_at"] = Instant.now().toString()
                    }

                remoteDataSource.updatePantryItem(placeholderId, updateMap)

                val updatedEntity =
                    existing.copy(
                        name = (updates["name"] as? String) ?: existing.name,
                        category = (updates["category"] as? String) ?: existing.category,
                        updatedAt = System.currentTimeMillis(),
                    )

                pantryDao.updatePantryItem(updatedEntity)
                true
            } catch (e: Exception) {
                throw Exception("Failed to update placeholder: ${e.message}", e)
            }
        }

    /**
     * Rimuove un placeholder dalla dispensa.
     *
     * @param placeholderId UUID del placeholder
     * @return true se rimosso, false se non trovato
     * @throws Exception in caso di errore
     */
    override suspend fun removePlaceholder(placeholderId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val existed = pantryDao.getPantryItemById(placeholderId) != null

                if (existed) {
                    remoteDataSource.deletePantryItem(placeholderId)
                    pantryDao.deletePantryItemById(placeholderId)
                }

                existed
            } catch (e: Exception) {
                throw Exception("Failed to remove placeholder: ${e.message}", e)
            }
        }

    // ===== RECEIPT INTEGRATION =====

    /**
     * Salva gli elementi estratti da uno scontrino nella dispensa.
     *
     * Logica:
     * - Items con receipt_id associato
     * - Batch insert su Supabase
     * - Cache in Room
     * - Tentativo di matching placeholder automatico
     * - Merge con placeholder existenti
     *
     * @param receiptId UUID dello scontrino
     * @param items elementi estratti da Gemini OCR
     * @throws Exception in caso di errore
     */
    override suspend fun saveReceiptItems(
        receiptId: String,
        items: List<PantryItem>,
    ) = withContext(Dispatchers.IO) {
        try {
            mergeReceiptItemsWithPantry(receiptId, items)
        } catch (e: Exception) {
            throw Exception("Failed to save receipt items: ${e.message}", e)
        }
    }

    /**
     * Merge automatico di items estratti da scontrino con placeholder existenti.
     *
     * Logica:
     * - Per ogni item da scontrino, cerca matching placeholder
     * - Converte placeholder → elemento reale se match trovato
     * - Crea nuovo elemento se nessun match
     *
     * @param receiptId UUID dello scontrino
     * @param receiptItems items estratti dallo scontrino
     * @throws Exception in caso di errore
     */
    override suspend fun mergeReceiptItemsWithPantry(
        receiptId: String,
        receiptItems: List<PantryItem>,
    ) = withContext(Dispatchers.IO) {
        try {
            val validReceiptId = if (receiptId.isBlank()) null else receiptId
            for (receiptItem in receiptItems) {
                val matchingPlaceholder = findMatchingPlaceholder(receiptItem.name)

                if (matchingPlaceholder != null) {
                    // Convert placeholder to real item
                    convertPlaceholderToRealItem(
                        matchingPlaceholder.id,
                        receiptItem.copy(
                            receiptId = validReceiptId,
                            isPlaceholder = false,
                        ),
                    )
                } else {
                    // Create new item if no match
                    addPantryItem(
                        receiptItem.copy(
                            receiptId = validReceiptId,
                            isPlaceholder = false,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to merge receipt items: ${e.message}", e)
        }
    }

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
    override suspend fun deduplicatePantryItems(): Int =
        withContext(Dispatchers.IO) {
            try {
                var removed = 0

                // Fetch all pantry items from local cache
                val allItems: List<PantryEntity> = pantryDao.getPantryItems(sessionProvider.getCurrentUserId()).first()

                // Group by (name, category)
                val grouped = allItems.groupBy { Pair(it.name, it.category) }

                for ((_, group) in grouped) {
                    if (group.size > 1) {
                        // Keep the most recent, delete others
                        val sorted = group.sortedByDescending { it.updatedAt }
                        for (i in 1 until sorted.size) {
                            try {
                                remoteDataSource.deletePantryItem(sorted[i].id)
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "PantryRepositoryImpl",
                                    "Eliminazione remota duplicato fallita per id=${sorted[i].id}: ${e.message}",
                                    e,
                                )
                            }
                            pantryDao.deletePantryItemById(sorted[i].id)
                            removed++
                        }
                    }
                }

                removed
            } catch (e: Exception) {
                throw Exception("Failed to deduplicate: ${e.message}", e)
            }
        }

    // ===== STATUS & EXPIRATION =====

    /**
     * Aggiorna lo stato di un elemento (active, consumed, discarded, expired).
     *
     * @param itemId UUID dell'elemento
     * @param status nuovo stato
     * @return true se successo, false se non trovato
     * @throws Exception in caso di errore
     */
    override suspend fun updateItemStatus(
        itemId: String,
        status: String,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val existing = pantryDao.getPantryItemById(itemId) ?: return@withContext false

                val updateMap: Map<String, Any> =
                    mapOf(
                        "status" to status,
                        "updated_at" to Instant.now().toString(),
                    )

                remoteDataSource.updatePantryItem(itemId, updateMap)

                val updatedEntity =
                    existing.copy(
                        status = status,
                        updatedAt = System.currentTimeMillis(),
                    )

                pantryDao.updatePantryItem(updatedEntity)
                true
            } catch (e: Exception) {
                throw Exception("Failed to update item status: ${e.message}", e)
            }
        }

    /**
     * Aggiorna la data di scadenza di un elemento.
     *
     * @param itemId UUID dell'elemento
     * @param expirationDate timestamp in millisecondi (ms from epoch)
     * @return true se successo, false se non trovato
     * @throws Exception in caso di errore
     */
    override suspend fun updateExpirationDate(
        itemId: String,
        expirationDate: Long,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val existing = pantryDao.getPantryItemById(itemId) ?: return@withContext false

                val expirationIso = Instant.ofEpochMilli(expirationDate).toString()
                val updateMap =
                    mapOf(
                        "expiration_date" to expirationIso,
                        "updated_at" to Instant.now().toString(),
                    )

                remoteDataSource.updatePantryItem(itemId, updateMap)

                val updatedEntity =
                    existing.copy(
                        expirationDate = expirationDate,
                        updatedAt = System.currentTimeMillis(),
                    )

                pantryDao.updatePantryItem(updatedEntity)
                true
            } catch (e: Exception) {
                throw Exception("Failed to update expiration date: ${e.message}", e)
            }
        }

    /**
     * Osserva gli elementi in scadenza entro una soglia di giorni.
     *
     * Logica:
     * - Calcola timestamp threshold (oggi + N giorni)
     * - Filtra items con expiration_date <= threshold
     * - Emette Flow aggiornato real-time
     * - Solo items non-placeholder, status "active"
     *
     * @param thresholdDays numero di giorni da oggi (es. 5 = scadono nei prossimi 5 giorni)
     * @return Flow della lista di items in scadenza
     */
    override fun getExpiringItems(thresholdDays: Int): Flow<List<PantryItem>> {
        val now = Instant.now()
        val threshold = now.plus(thresholdDays.toLong(), ChronoUnit.DAYS).toEpochMilli()

        return pantryDao
            .getExpiringItems(sessionProvider.getCurrentUserId(), threshold)
            .map { entities ->
                entities
                    .filter { !it.isPlaceholder && it.status == "ACTIVE" }
                    .map { entity -> entityToDomain(entity) }
            }
    }

    // ===== NOTIFICATIONS =====

    override suspend fun getItemsDueForNotification(windowEnd: Long): List<PantryItem> =
        withContext(Dispatchers.IO) {
            pantryDao
                .getItemsDueForNotification(sessionProvider.getCurrentUserId(), windowEnd)
                .map { entityToDomain(it) }
        }

    override suspend fun markItemsNotified(ids: List<String>) =
        withContext(Dispatchers.IO) {
            pantryDao.markAllAsNotified(ids, System.currentTimeMillis())
        }

    // ===== RECIPE INTEGRATION =====

    /**
     * Deduce una quantità dalla dispensa per un ingrediente di ricetta, individuato per nome.
     *
     * Logica:
     * - Cerca il primo elemento non-placeholder che corrisponde al nome (fuzzy match)
     * - Se nessun match, non fa nulla (fail-safe)
     * - Se newQuantity <= 0, elimina l'elemento; altrimenti aggiorna la quantità
     */
    override suspend fun deductIngredientQuantity(
        ingredientName: String,
        amountToDeduct: Double,
    ) = withContext(Dispatchers.IO) {
        try {
            val items = pantryDao.getPantryItems(sessionProvider.getCurrentUserId()).first()
            val match =
                items.firstOrNull { entity ->
                    !entity.isPlaceholder && ingredientMatchesPantryName(ingredientName, entity.name)
                } ?: return@withContext

            val newQuantity = match.quantity - amountToDeduct
            if (newQuantity <= 0) {
                deletePantryItem(match.id)
            } else {
                updatePantryItem(match.id, entityToDomain(match).copy(quantity = newQuantity))
            }
        } catch (e: Exception) {
            throw Exception("Failed to deduct ingredient quantity: ${e.message}", e)
        }
    }

    // ===== HELPERS =====

    /**
     * Converte un'entità Room in un modello di dominio.
     *
     * @param entity entità Room
     * @return modello PantryItem di dominio
     */
    private fun entityToDomain(entity: PantryEntity): PantryItem =
        PantryItem(
            id = entity.id,
            userId = entity.userId,
            receiptId = entity.receiptId,
            name = entity.name,
            category = entity.category,
            categoryKey = entity.categoryKey,
            isPlaceholder = entity.isPlaceholder,
            status = entity.status,
            quantity = entity.quantity,
            unit = entity.unit,
            expirationDate = entity.expirationDate,
        )
}
