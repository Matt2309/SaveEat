package com.mattiamularoni.saveeat.features.recipes.data.repository

import com.mattiamularoni.saveeat.features.recipes.data.local.RecipeDao
import com.mattiamularoni.saveeat.features.recipes.data.mapper.FavoriteRecipeMapper
import com.mattiamularoni.saveeat.features.recipes.data.mapper.RecipeMapper
import com.mattiamularoni.saveeat.features.recipes.data.remote.FavoriteRecipeDto
import com.mattiamularoni.saveeat.features.recipes.data.remote.RecipeRemoteDataSource
import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe
import com.mattiamularoni.saveeat.features.recipes.domain.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * Implementazione di RecipeRepository con sincronizzazione Supabase e caching Room.
 *
 * Responsabilità:
 * - Orchestrare remote datasource + local cache
 * - Logica business (filtraggio, matching ingredienti)
 * - Offline-first pattern: fetch remoto -> cache locale -> query su Flow
 * - Gestione errori e retry
 * - Operare su Dispatchers.IO (async, no Main Thread blocking)
 */
class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao,
    private val remoteDataSource: RecipeRemoteDataSource
) : RecipeRepository {

    // ===== OBSERVE & SYNC =====

    /**
     * Osserva tutte le ricette disponibili con aggiornamenti real-time.
     *
     * @return Flow della lista ricette aggiornato
     */
    override fun observeRecipes(): Flow<List<Recipe>> {
        return recipeDao
            .getAllRecipes()
            .map { entities -> RecipeMapper.entitiesToDomain(entities) }
    }

    /**
     * Sincronizza le ricette con Supabase.
     *
     * @return numero di ricette sincronizzate
     */
    override suspend fun syncRecipes(): Int = withContext(Dispatchers.IO) {
        try {
            val remoteRecipes = remoteDataSource.getRecipes()
            val existingImageUrls = recipeDao.getAllImageUrls().associate { it.id to it.imageUrl }
            val entities = RecipeMapper.dtosToEntities(remoteRecipes).map { entity ->
                entity.copy(imageUrl = existingImageUrls[entity.id])
            }
            recipeDao.insertRecipes(entities)
            remoteRecipes.size
        } catch (e: Exception) {
            throw Exception("Failed to sync recipes: ${e.message}", e)
        }
    }

    override suspend fun updateImageUrl(recipeId: String, imageUrl: String) =
        withContext(Dispatchers.IO) {
            recipeDao.updateImageUrl(recipeId, imageUrl)
        }

    // ===== BASIC CRUD =====

    /**
     * Recupera una singola ricetta per ID.
     *
     * @param recipeId UUID della ricetta
     * @return Recipe se trovata, null altrimenti
     */
    override suspend fun getRecipeById(recipeId: String): Recipe? =
        withContext(Dispatchers.IO) {
            try {
                recipeDao.getRecipeById(recipeId)?.let { entity ->
                    RecipeMapper.entityToDomain(entity)
                }
            } catch (e: Exception) {
                throw Exception("Failed to get recipe by id: ${e.message}", e)
            }
        }

    /**
     * Cerca ricette per query di testo.
     *
     * @param query stringa di ricerca
     * @return lista di ricette matching
     */
    override suspend fun searchRecipes(query: String): List<Recipe> =
        withContext(Dispatchers.IO) {
            try {
                val dtos = remoteDataSource.searchRecipes(query)
                RecipeMapper.dtosToDomain(dtos)
            } catch (e: Exception) {
                throw Exception("Failed to search recipes: ${e.message}", e)
            }
        }

    /**
     * Filtra ricette per tag.
     *
     * @param tags lista di tag da filtrare
     * @return lista di ricette matching
     */
    override suspend fun getRecipesByTags(tags: List<String>): List<Recipe> =
        withContext(Dispatchers.IO) {
            try {
                val dtos = remoteDataSource.getRecipesByTags(tags)
                RecipeMapper.dtosToDomain(dtos)
            } catch (e: Exception) {
                throw Exception("Failed to fetch recipes by tags: ${e.message}", e)
            }
        }

    // ===== INTELLIGENT GENERATION =====

    /**
     * Genera ricette intelligenti utilizzando ingredienti della dispensa.
     *
     * @param ingredients lista di ingredienti disponibili
     * @param preferences mappa di preferenze
     * @return lista di ricette generate
     */
    override suspend fun generateRecipes(
        ingredients: List<String>,
        preferences: Map<String, Any>
    ): List<Recipe> =
        withContext(Dispatchers.IO) {
            try {
                val dtos = remoteDataSource.generateRecipe(ingredients, preferences)
                val entities = RecipeMapper.dtosToEntities(dtos)
                recipeDao.insertRecipes(entities)
                RecipeMapper.dtosToDomain(dtos)
            } catch (e: Exception) {
                throw Exception("Failed to generate recipes: ${e.message}", e)
            }
        }

    /**
     * Suggerisce ricette basate su ingredienti in scadenza.
     *
     * @param expiringItems lista di nomi ingredienti in scadenza
     * @param preferences mappa di preferenze
     * @return lista di ricette suggerite
     */
    override suspend fun getSuggestedRecipesForExpiringItems(
        expiringItems: List<String>,
        preferences: Map<String, Any>
    ): List<Recipe> =
        withContext(Dispatchers.IO) {
            try {
                val prefs = preferences.toMutableMap()
                prefs["focus"] = "use_expiring_items"
                val dtos = remoteDataSource.generateRecipe(expiringItems, prefs)
                val entities = RecipeMapper.dtosToEntities(dtos)
                recipeDao.insertRecipes(entities)
                RecipeMapper.dtosToDomain(dtos)
            } catch (e: Exception) {
                throw Exception("Failed to get suggested recipes: ${e.message}", e)
            }
        }

    /**
     * Suggerisce ricette stagionali.
     *
     * @param season stagione corrente
     * @param preferences mappa di preferenze
     * @return lista di ricette stagionali
     */
    override suspend fun getSeasonalRecipes(
        season: String,
        preferences: Map<String, Any>
    ): List<Recipe> =
        withContext(Dispatchers.IO) {
            try {
                val prefs = preferences.toMutableMap()
                prefs["season"] = season
                val dtos = remoteDataSource.generateRecipe(emptyList(), prefs)
                val entities = RecipeMapper.dtosToEntities(dtos)
                recipeDao.insertRecipes(entities)
                RecipeMapper.dtosToDomain(dtos)
            } catch (e: Exception) {
                throw Exception("Failed to get seasonal recipes: ${e.message}", e)
            }
        }

    // ===== FAVORITE MANAGEMENT =====

    /**
     * Osserva le ricette preferite di un utente con aggiornamenti real-time.
     *
     * @param userId UUID dell'utente
     * @return Flow della lista ricette preferite
     */
    override fun observeFavoriteRecipes(userId: String): Flow<List<Recipe>> {
        return recipeDao
            .getFavoriteRecipesByUser(userId)
            .map { entities -> RecipeMapper.entitiesToDomain(entities) }
    }

    /**
     * Recupera le ricette preferite di un utente.
     *
     * @param userId UUID dell'utente
     * @return lista di ricette preferite
     */
    override suspend fun getFavoriteRecipes(userId: String): List<Recipe> =
        withContext(Dispatchers.IO) {
            try {
                syncFavoriteRecipes(userId)
                val entities = recipeDao.getFavoriteRecipesListByUser(userId)
                RecipeMapper.entitiesToDomain(entities)
            } catch (e: Exception) {
                val entities = recipeDao.getFavoriteRecipesListByUser(userId)
                RecipeMapper.entitiesToDomain(entities)
            }
        }

    /**
     * Aggiunge una ricetta ai preferiti.
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return true se successo
     */
    override suspend fun addFavoriteRecipe(userId: String, recipeId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val now = Instant.now().toString()
                val favoriteDto = FavoriteRecipeDto(userId, recipeId, now)
                val favoriteEntity = FavoriteRecipeMapper.dtoToEntity(favoriteDto)
                val success = remoteDataSource.addFavoriteRecipe(favoriteDto)
                if (success) {
                    recipeDao.addFavoriteRecipe(favoriteEntity)
                }
                success
            } catch (e: Exception) {
                throw Exception("Failed to add favorite recipe: ${e.message}", e)
            }
        }

    /**
     * Rimuove una ricetta dai preferiti.
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return true se rimosso
     */
    override suspend fun removeFavoriteRecipe(userId: String, recipeId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                remoteDataSource.removeFavoriteRecipe(userId, recipeId)
                val deletedCount = recipeDao.removeFavoriteRecipe(userId, recipeId)
                deletedCount > 0
            } catch (e: Exception) {
                throw Exception("Failed to remove favorite recipe: ${e.message}", e)
            }
        }

    /**
     * Verifica se una ricetta è nei preferiti dell'utente.
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return Flow che emette true se preferita, false altrimenti
     */
    override fun isFavoriteRecipe(userId: String, recipeId: String): Flow<Boolean> {
        return recipeDao.isFavoriteRecipe(userId, recipeId)
    }

    /**
     * Sincronizza i preferiti dell'utente con Supabase.
     *
     * @param userId UUID dell'utente
     * @return numero di preferiti sincronizzati
     */
    override suspend fun syncFavoriteRecipes(userId: String): Int =
        withContext(Dispatchers.IO) {
            try {
                val remoteFavorites = remoteDataSource.getFavoriteRecipes(userId)
                val entities = FavoriteRecipeMapper.dtosToEntities(remoteFavorites)
                entities.forEach { recipeDao.addFavoriteRecipe(it) }
                remoteFavorites.size
            } catch (e: Exception) {
                throw Exception("Failed to sync favorite recipes: ${e.message}", e)
            }
        }
}
