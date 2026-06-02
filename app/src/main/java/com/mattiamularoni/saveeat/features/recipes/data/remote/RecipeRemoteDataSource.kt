package com.mattiamularoni.saveeat.features.recipes.data.remote

/**
 * Interface astratta per le operazioni remote su Supabase Postgrest.
 *
 * Responsabilità:
 * - Fetch/upload ricette da/verso recipes table
 * - Gestione favorite_recipes table
 * - Query specifiche (search, filtri tag, generazione AI)
 */
interface RecipeRemoteDataSource {
    /**
     * Recupera tutte le ricette disponibili da Supabase.
     *
     * @return lista di DTO dalla risposta Postgrest
     * @throws Exception in caso di errore rete o parsing
     */
    suspend fun getRecipes(): List<RecipeDto>

    /**
     * Recupera una singola ricetta per ID da Supabase.
     *
     * @param recipeId UUID della ricetta
     * @return DTO della ricetta, null se non trovata
     * @throws Exception in caso di errore
     */
    suspend fun getRecipeById(recipeId: String): RecipeDto?

    /**
     * Cerca ricette per query di testo (titolo, ingredienti).
     *
     * @param query stringa di ricerca
     * @return lista di DTO matching
     * @throws Exception in caso di errore
     */
    suspend fun searchRecipes(query: String): List<RecipeDto>

    /**
     * Filtra ricette per tag.
     *
     * @param tags lista di tag da filtrare
     * @return lista di DTO matching
     * @throws Exception in caso di errore
     */
    suspend fun getRecipesByTags(tags: List<String>): List<RecipeDto>

    /**
     * Genera ricette intelligenti usando ingredienti della dispensa.
     *
     * Effettua una chiamata al servizio AI (Gemini o backend API)
     * per generare ricette personalizzate basate sugli ingredienti forniti.
     *
     * @param ingredients lista di ingredienti disponibili
     * @param preferences mappa di preferenze culinarie (dieta, allergie, ecc.)
     * @return lista di DTO delle ricette generate
     * @throws Exception in caso di errore rete o timeout AI
     */
    suspend fun generateRecipe(
        ingredients: List<String>,
        preferences: Map<String, Any>
    ): List<RecipeDto>

    /**
     * Aggiunge una ricetta ai preferiti dell'utente.
     *
     * @param favoriteDto DTO con user_id e recipe_id
     * @return true se successo, false se già preferita
     * @throws Exception in caso di errore
     */
    suspend fun addFavoriteRecipe(favoriteDto: FavoriteRecipeDto): Boolean

    /**
     * Rimuove una ricetta dai preferiti dell'utente.
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return true se rimosso, false se non trovato
     * @throws Exception in caso di errore
     */
    suspend fun removeFavoriteRecipe(userId: String, recipeId: String): Boolean

    /**
     * Recupera le ricette preferite di un utente da Supabase.
     *
     * @param userId UUID dell'utente
     * @return lista di DTO delle ricette preferite
     * @throws Exception in caso di errore
     */
    suspend fun getFavoriteRecipes(userId: String): List<FavoriteRecipeDto>

    /**
     * Verifica se una ricetta è nei preferiti dell'utente.
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return true se preferita, false altrimenti
     * @throws Exception in caso di errore
     */
    suspend fun isFavoriteRecipe(userId: String, recipeId: String): Boolean
}
