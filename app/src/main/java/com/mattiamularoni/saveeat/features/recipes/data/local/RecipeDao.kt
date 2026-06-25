package com.mattiamularoni.saveeat.features.recipes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO per operazioni su ricette nel database Room locale.
 *
 * Responsabilità:
 * - Query su recipes table
 * - Query su favorite_recipes table
 * - Operazioni di insert/update/delete con caching
 * - Ritornare Flow per osservare cambiamenti in tempo reale
 */
@Dao
interface RecipeDao {
    /**
     * Osserva tutte le ricette in cache con aggiornamenti real-time.
     *
     * @return Flow della lista ricette aggiornato
     */
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    /**
     * Osserva una singola ricetta per ID.
     *
     * @param recipeId UUID della ricetta
     * @return Flow della ricetta, emette null se non trovata
     */
    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    fun observeRecipeById(recipeId: String): Flow<RecipeEntity?>

    /**
     * Recupera una singola ricetta per ID (one-shot).
     *
     * @param recipeId UUID della ricetta
     * @return RecipeEntity se trovata, null altrimenti
     */
    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    suspend fun getRecipeById(recipeId: String): RecipeEntity?

    /**
     * Inserisce una nuova ricetta in cache.
     *
     * Strategia REPLACE: se ricetta già esiste (stesso ID),
     * i dati vengono sovrascritti.
     *
     * @param recipe entità ricetta da inserire
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    /**
     * Inserisce multiple ricette in cache (batch operation).
     *
     * Strategia REPLACE: utile per sincronizzazione da Supabase.
     *
     * @param recipes lista di entità ricette da inserire
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    /**
     * Elimina una ricetta dalla cache.
     *
     * @param recipeId UUID della ricetta da eliminare
     * @return numero di righe eliminate
     */
    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipe(recipeId: String): Int

    /**
     * Elimina tutte le ricette dalla cache.
     *
     * Operazione di pulizia cache per sincronizzazione forzata.
     *
     * @return numero di righe eliminate
     */
    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes(): Int

    /**
     * Osserva le ricette preferite di un utente con aggiornamenti real-time.
     *
     * @param userId UUID dell'utente
     * @return Flow della lista ricette preferite
     */
    @Query(
        """
        SELECT r.* FROM recipes r
        INNER JOIN favorite_recipes fr ON r.id = fr.recipeId
        WHERE fr.userId = :userId
        ORDER BY fr.savedAt DESC
        """,
    )
    fun getFavoriteRecipesByUser(userId: String): Flow<List<RecipeEntity>>

    /**
     * Recupera le ricette preferite di un utente (one-shot).
     *
     * @param userId UUID dell'utente
     * @return Lista di ricette preferite
     */
    @Query(
        """
        SELECT r.* FROM recipes r
        INNER JOIN favorite_recipes fr ON r.id = fr.recipeId
        WHERE fr.userId = :userId
        ORDER BY fr.savedAt DESC
        """,
    )
    suspend fun getFavoriteRecipesListByUser(userId: String): List<RecipeEntity>

    /**
     * Inserisce una ricetta nei preferiti.
     *
     * @param favorite entità favorite_recipe da inserire
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteRecipe(favorite: FavoriteRecipeEntity)

    /**
     * Rimuove una ricetta dai preferiti.
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return numero di righe eliminate
     */
    @Query(
        "DELETE FROM favorite_recipes WHERE userId = :userId AND recipeId = :recipeId",
    )
    suspend fun removeFavoriteRecipe(
        userId: String,
        recipeId: String,
    ): Int

    /**
     * Verifica se una ricetta è nei preferiti di un utente.
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return Flow che emette true se preferita, false altrimenti
     */
    @Query(
        "SELECT COUNT(*) > 0 FROM favorite_recipes WHERE userId = :userId AND recipeId = :recipeId",
    )
    fun isFavoriteRecipe(
        userId: String,
        recipeId: String,
    ): Flow<Boolean>

    /**
     * Elimina tutti i preferiti di un utente (es. logout).
     *
     * @param userId UUID dell'utente
     * @return numero di righe eliminate
     */
    @Query("DELETE FROM favorite_recipes WHERE userId = :userId")
    suspend fun deleteFavoritesByUser(userId: String): Int
}
