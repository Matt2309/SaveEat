package com.mattiamularoni.saveeat.features.recipes.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface per operazioni sulle ricette.
 *
 * Responsabilità:
 * - Orchestrare fetch dati Supabase + caching Room
 * - Esporre dati come Flow per streaming real-time
 * - Supportare refresh manuale
 * - Gestire favorite recipes e generazione AI
 * - Fornire ricette intelligenti basate su ingredienti dispensa
 */
interface RecipeRepository {
    // ===== OBSERVE & SYNC =====

    /**
     * Osserva tutte le ricette disponibili con aggiornamenti real-time.
     *
     * Logica:
     * - Fetch iniziale da Supabase
     * - Cache in Room
     * - Flow emette ogni cambio Room-local
     * - Background sync in progress
     *
     * @return Flow della lista ricette aggiornato
     */
    fun observeRecipes(): Flow<List<Recipe>>

    /**
     * Sincronizza le ricette con Supabase.
     *
     * Logica:
     * - Fetch ricette da Supabase
     * - Cache in Room (upsert con REPLACE strategy)
     * - Ritorna numero ricette sincronizzate
     *
     * Operazione: Dispatchers.IO (remote fetch)
     *
     * @return numero di ricette sincronizzate
     * @throws Exception in caso di errore rete
     */
    suspend fun syncRecipes(): Int

    // ===== BASIC CRUD =====

    /**
     * Recupera una singola ricetta per ID.
     *
     * Legge dalla cache Room locale.
     *
     * @param recipeId UUID della ricetta
     * @return Recipe se trovata, null altrimenti
     */
    suspend fun getRecipeById(recipeId: String): Recipe?

    /**
     * Cerca ricette per query di testo.
     *
     * Ricerca su titolo e ingredienti. Operazione remota.
     *
     * @param query stringa di ricerca
     * @return lista di ricette matching
     * @throws Exception in caso di errore
     */
    suspend fun searchRecipes(query: String): List<Recipe>

    /**
     * Filtra ricette per tag.
     *
     * @param tags lista di tag da filtrare
     * @return lista di ricette matching
     * @throws Exception in caso di errore
     */
    suspend fun getRecipesByTags(tags: List<String>): List<Recipe>

    // ===== INTELLIGENT GENERATION =====

    /**
     * Genera ricette intelligenti utilizzando ingredienti della dispensa.
     *
     * Effettua una chiamata al servizio AI (Gemini o backend API)
     * per generare ricette personalizzate basate sugli ingredienti forniti
     * e alle preferenze culinarie dell'utente.
     *
     * @param ingredients lista di ingredienti disponibili nella dispensa
     * @param preferences mappa di preferenze (dieta, allergie, tempo massimo, ecc.)
     * @return lista di ricette generate dal sistema AI
     * @throws Exception in caso di errore rete o timeout AI
     */
    suspend fun generateRecipes(
        ingredients: List<String>,
        preferences: Map<String, Any> = emptyMap(),
    ): List<Recipe>

    /**
     * Suggerisce ricette basate su ingredienti in scadenza.
     *
     * Utilizza l'AI per generare ricette che aiutino a utilizzare
     * ingredienti prossimi alla scadenza, minimizzando sprechi alimentari.
     *
     * @param expiringItems lista di item della dispensa con scadenza imminente
     * @param preferences mappa di preferenze culinarie
     * @return lista di ricette suggerite per gli item in scadenza
     * @throws Exception in caso di errore
     */
    suspend fun getSuggestedRecipesForExpiringItems(
        expiringItems: List<String>,
        preferences: Map<String, Any> = emptyMap(),
    ): List<Recipe>

    /**
     * Suggerisce ricette stagionali.
     *
     * Ritorna ricette ottimizzate per il periodo/stagione corrente.
     *
     * @param season stagione corrente (es. "primavera", "estate", "autunno", "inverno")
     * @param preferences mappa di preferenze culinarie
     * @return lista di ricette stagionali
     * @throws Exception in caso di errore
     */
    suspend fun getSeasonalRecipes(
        season: String,
        preferences: Map<String, Any> = emptyMap(),
    ): List<Recipe>

    // ===== FAVORITE MANAGEMENT =====

    /**
     * Osserva le ricette preferite di un utente con aggiornamenti real-time.
     *
     * Logica:
     * - Fetch iniziale da Supabase
     * - Cache in Room
     * - Flow emette aggiornamenti real-time
     *
     * @param userId UUID dell'utente
     * @return Flow della lista ricette preferite
     */
    fun observeFavoriteRecipes(userId: String): Flow<List<Recipe>>

    /**
     * Recupera le ricette preferite di un utente (versione singola, non Flow).
     *
     * @param userId UUID dell'utente
     * @return lista di ricette preferite
     * @throws Exception in caso di errore
     */
    suspend fun getFavoriteRecipes(userId: String): List<Recipe>

    /**
     * Aggiunge una ricetta ai preferiti dell'utente.
     *
     * Logica:
     * - Insert in Supabase favorite_recipes
     * - Cache in Room
     * - Ritorna true se successo
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return true se aggiunto, false se già preferita
     * @throws Exception in caso di errore
     */
    suspend fun addFavoriteRecipe(
        userId: String,
        recipeId: String,
    ): Boolean

    /**
     * Rimuove una ricetta dai preferiti dell'utente.
     *
     * Logica:
     * - Delete da Supabase favorite_recipes
     * - Delete da Room
     * - Ritorna true se rimosso
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return true se rimosso, false se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun removeFavoriteRecipe(
        userId: String,
        recipeId: String,
    ): Boolean

    /**
     * Verifica se una ricetta è nei preferiti dell'utente.
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return Flow che emette true se preferita, false altrimenti
     */
    fun isFavoriteRecipe(
        userId: String,
        recipeId: String,
    ): Flow<Boolean>

    /**
     * Sincronizza i preferiti dell'utente con Supabase.
     *
     * @param userId UUID dell'utente
     * @return numero di preferiti sincronizzati
     * @throws Exception in caso di errore
     */
    suspend fun syncFavoriteRecipes(userId: String): Int
}
