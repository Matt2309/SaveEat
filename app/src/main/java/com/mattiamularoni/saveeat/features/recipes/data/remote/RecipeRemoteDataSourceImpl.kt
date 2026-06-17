package com.mattiamularoni.saveeat.features.recipes.data.remote

import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

/**
 * Implementazione di RecipeRemoteDataSource usando Supabase Postgrest.
 *
 * Responsabilità:
 * - Eseguire query su Postgrest per recipes e favorite_recipes
 * - Mappare risposta JSON in DTO
 * - Gestire errori di rete e parsing
 * - Operare su Dispatchers.IO (non bloccare Main Thread)
 * - Orchestrare chiamate AI/Gemini per generazione ricette
 *
 * NOTE: MVP implementation - Postgrest query builder da integrare quando plugin disponibile.
 */
class RecipeRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient,
    private val geminiRecipeDataSource: GeminiRecipeDataSource
) : RecipeRemoteDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Recupera tutte le ricette disponibili da Supabase.
     *
     * Query Postgrest: SELECT * FROM recipes ORDER BY created_at DESC
     *
     * @return lista di DTO
     * @throws Exception se parsing JSON fallisce o errore rete
     */
    override suspend fun getRecipes(): List<RecipeDto> =
        withContext(Dispatchers.IO) {
            try {
                // When Postgrest plugin is available:
                // supabaseClient.postgrest["recipes"]
                //     .select()
                //     .order("created_at", order = Order.DESCENDING)
                //     .decodeList<RecipeDto>()
                 
                // For MVP: return empty list until Postgrest SDK integration
                emptyList()
            } catch (e: Exception) {
                throw Exception("Failed to fetch recipes: ${e.message}", e)
            }
        }

    /**
     * Recupera una singola ricetta per ID.
     *
     * Query Postgrest: SELECT * FROM recipes WHERE id = {recipeId}
     *
     * @param recipeId UUID della ricetta
     * @return DTO della ricetta
     */
    override suspend fun getRecipeById(recipeId: String): RecipeDto? =
        withContext(Dispatchers.IO) {
            try {
                // When Postgrest plugin is available:
                // supabaseClient.postgrest["recipes"]
                //     .select()
                //     .eq("id", recipeId)
                //     .decodeSingle<RecipeDto>()
                 
                // For MVP: return null until Postgrest SDK integration
                null
            } catch (e: Exception) {
                throw Exception("Failed to fetch recipe by id: ${e.message}", e)
            }
        }

    /**
     * Cerca ricette per query di testo.
     *
     * Query Postgrest: SELECT * FROM recipes WHERE title ILIKE '%{query}%'
     * OR ingredients ILIKE '%{query}%'
     *
     * @param query stringa di ricerca
     * @return lista di DTO matching
     */
    override suspend fun searchRecipes(query: String): List<RecipeDto> =
        withContext(Dispatchers.IO) {
            try {
                // When Postgrest plugin is available:
                // supabaseClient.postgrest["recipes"]
                //     .select()
                //     .or("title.ilike.%${query}%,ingredients.ilike.%${query}%")
                //     .decodeList<RecipeDto>()
                 
                // For MVP: return empty list until Postgrest SDK integration
                emptyList()
            } catch (e: Exception) {
                throw Exception("Failed to search recipes: ${e.message}", e)
            }
        }

    /**
     * Filtra ricette per tag.
     *
     * Query Postgrest: SELECT * FROM recipes WHERE tags @@ '{tag1,tag2}'
     * (utilizzo di ARRAY operators di PostgreSQL)
     *
     * @param tags lista di tag da filtrare
     * @return lista di DTO matching
     */
    override suspend fun getRecipesByTags(tags: List<String>): List<RecipeDto> =
        withContext(Dispatchers.IO) {
            try {
                if (tags.isEmpty()) return@withContext emptyList()
                 
                // When Postgrest plugin is available with array support:
                // Build OR condition for multiple tags
                // supabaseClient.postgrest["recipes"]
                //     .select()
                //     .or(tags.map { "tags.ilike.%${it}%" }.joinToString(","))
                //     .decodeList<RecipeDto>()
                 
                // For MVP: return empty list until Postgrest SDK integration
                emptyList()
            } catch (e: Exception) {
                throw Exception("Failed to fetch recipes by tags: ${e.message}", e)
            }
        }

    /**
     * Genera ricette intelligenti utilizzando ingredienti della dispensa.
     *
     * Orchestrazione:
     * - Invia ingredienti + preferenze a Gemini API (o backend AI endpoint)
     * - Riceve lista ricette generate
     * - Mapperà in DTO per uniformità layer
     *
     * @param ingredients lista di ingredienti (es. ["pollo", "riso", "aglio"])
     * @param preferences mappa preferenze (es. {"dieta": "vegetariana", "tempo_max": "30"})
     * @return lista di DTO ricette generate
     */
    override suspend fun generateRecipe(
        ingredients: List<String>,
        preferences: Map<String, Any>
    ): List<RecipeDto> =
        withContext(Dispatchers.IO) {
            try {
                val jsonString = geminiRecipeDataSource.generateRecipes(ingredients, preferences)
                val geminiDtos = json.decodeFromString<List<GeminiRecipeDto>>(jsonString)
                val now = Instant.now()
                geminiDtos.map { geminiDto ->
                    RecipeDto(
                        id = UUID.randomUUID().toString(),
                        title = geminiDto.title,
                        instructions = geminiDto.instructions,
                        ingredients = json.encodeToString(geminiDto.ingredients),
                        prepTimeMinutes = geminiDto.prepTimeMinutes,
                        tags = geminiDto.tags.joinToString(","),
                        createdAt = now.toString()
                    )
                }
            } catch (e: Exception) {
                throw Exception("Failed to generate recipes: ${e.message}", e)
            }
        }

    /**
     * Aggiunge una ricetta ai preferiti dell'utente.
     *
     * Query Postgrest: INSERT INTO favorite_recipes (user_id, recipe_id, saved_at)
     * VALUES ({userId}, {recipeId}, NOW())
     *
     * @param favoriteDto DTO con user_id e recipe_id
     * @return true se successo
     */
    override suspend fun addFavoriteRecipe(favoriteDto: FavoriteRecipeDto): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // When Postgrest plugin is available:
                // supabaseClient.postgrest["favorite_recipes"]
                //     .insert(favoriteDto)
                // true
                 
                // Handle duplicate key constraint gracefully and return false if already favorited
                // On error with unique constraint violation: return false
                // On success: return true
                 
                // For MVP: return true until Postgrest SDK integration
                true
            } catch (e: Exception) {
                throw Exception("Failed to add favorite recipe: ${e.message}", e)
            }
        }

    /**
     * Rimuove una ricetta dai preferiti dell'utente.
     *
     * Query Postgrest: DELETE FROM favorite_recipes
     * WHERE user_id = {userId} AND recipe_id = {recipeId}
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return true se rimosso, false se non trovato
     */
    override suspend fun removeFavoriteRecipe(userId: String, recipeId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // When Postgrest plugin is available:
                // supabaseClient.postgrest["favorite_recipes"]
                //     .delete()
                //     .eq("user_id", userId)
                //     .eq("recipe_id", recipeId)
                // true
                 
                // For MVP: return true until Postgrest SDK integration
                true
            } catch (e: Exception) {
                throw Exception("Failed to remove favorite recipe: ${e.message}", e)
            }
        }

    /**
     * Recupera le ricette preferite di un utente.
     *
     * Query Postgrest: SELECT fr.*, r.* FROM favorite_recipes fr
     * JOIN recipes r ON fr.recipe_id = r.id
     * WHERE fr.user_id = {userId}
     *
     * @param userId UUID dell'utente
     * @return lista di DTO delle ricette preferite
     */
    override suspend fun getFavoriteRecipes(userId: String): List<FavoriteRecipeDto> =
        withContext(Dispatchers.IO) {
            try {
                // When Postgrest plugin is available with relation support:
                // supabaseClient.postgrest["favorite_recipes"]
                //     .select(columns = "*, recipes(*)")
                //     .eq("user_id", userId)
                //     .decodeList<FavoriteRecipeDto>()
                 
                // Alternative simpler query (just favorite_recipes, join in app):
                // supabaseClient.postgrest["favorite_recipes"]
                //     .select()
                //     .eq("user_id", userId)
                //     .decodeList<FavoriteRecipeDto>()
                 
                // For MVP: return empty list until Postgrest SDK integration
                emptyList()
            } catch (e: Exception) {
                throw Exception("Failed to fetch favorite recipes: ${e.message}", e)
            }
        }

    /**
     * Verifica se una ricetta è nei preferiti dell'utente.
     *
     * Query Postgrest: SELECT COUNT(*) FROM favorite_recipes
     * WHERE user_id = {userId} AND recipe_id = {recipeId}
     *
     * @param userId UUID dell'utente
     * @param recipeId UUID della ricetta
     * @return true se preferita
     */
    override suspend fun isFavoriteRecipe(userId: String, recipeId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // When Postgrest plugin is available:
                // supabaseClient.postgrest["favorite_recipes"]
                //     .select("count=exact")
                //     .eq("user_id", userId)
                //     .eq("recipe_id", recipeId)
                //     .decodeSingle<Count>()
                //     .count > 0
                 
                // Or simpler:
                // val result = supabaseClient.postgrest["favorite_recipes"]
                //     .select()
                //     .eq("user_id", userId)
                //     .eq("recipe_id", recipeId)
                //     .decodeList<FavoriteRecipeDto>()
                // result.isNotEmpty()
                 
                // For MVP: return false until Postgrest SDK integration
                false
            } catch (e: Exception) {
                throw Exception("Failed to check favorite recipe: ${e.message}", e)
            }
        }
}
