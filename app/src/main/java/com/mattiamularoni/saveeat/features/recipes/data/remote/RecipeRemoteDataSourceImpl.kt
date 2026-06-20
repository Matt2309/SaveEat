package com.mattiamularoni.saveeat.features.recipes.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

class RecipeRemoteDataSourceImpl(
    private val supabaseClient: SupabaseClient,
    private val geminiRecipeDataSource: GeminiRecipeDataSource
) : RecipeRemoteDataSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getRecipes(): List<RecipeDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("recipes")
                    .select()
                    .decodeList<RecipeDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch recipes: ${e.message}", e)
            }
        }

    override suspend fun getRecipeById(recipeId: String): RecipeDto? =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("recipes")
                    .select {
                        filter {
                            eq("id", recipeId)
                        }
                    }
                    .decodeList<RecipeDto>()
                    .firstOrNull()
            } catch (e: Exception) {
                throw Exception("Failed to fetch recipe by id: ${e.message}", e)
            }
        }

    override suspend fun searchRecipes(query: String): List<RecipeDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("recipes")
                    .select {
                        filter {
                            ilike("title", "%$query%")
                        }
                    }
                    .decodeList<RecipeDto>()
            } catch (e: Exception) {
                throw Exception("Failed to search recipes: ${e.message}", e)
            }
        }

    override suspend fun getRecipesByTags(tags: List<String>): List<RecipeDto> =
        withContext(Dispatchers.IO) {
            try {
                if (tags.isEmpty()) return@withContext emptyList()
                supabaseClient
                    .from("recipes")
                    .select {
                        filter {
                            ilike("tags", "%${tags.first()}%")
                        }
                    }
                    .decodeList<RecipeDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch recipes by tags: ${e.message}", e)
            }
        }

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

    override suspend fun insertRecipes(dtos: List<RecipeDto>) =
        withContext(Dispatchers.IO) {
            try {
                if (dtos.isEmpty()) return@withContext
                supabaseClient.from("recipes").insert(dtos)
            } catch (e: Exception) {
                throw Exception("Failed to insert recipes: ${e.message}", e)
            }
        }

    override suspend fun addFavoriteRecipe(favoriteDto: FavoriteRecipeDto): Boolean =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("favorite_recipes")
                    .insert(favoriteDto)
                true
            } catch (e: Exception) {
                // Unique constraint violation means already favorited
                false
            }
        }

    override suspend fun removeFavoriteRecipe(userId: String, recipeId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("favorite_recipes")
                    .delete {
                        filter {
                            eq("user_id", userId)
                            eq("recipe_id", recipeId)
                        }
                    }
                true
            } catch (e: Exception) {
                throw Exception("Failed to remove favorite recipe: ${e.message}", e)
            }
        }

    override suspend fun getFavoriteRecipes(userId: String): List<FavoriteRecipeDto> =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("favorite_recipes")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<FavoriteRecipeDto>()
            } catch (e: Exception) {
                throw Exception("Failed to fetch favorite recipes: ${e.message}", e)
            }
        }

    override suspend fun isFavoriteRecipe(userId: String, recipeId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                supabaseClient
                    .from("favorite_recipes")
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("recipe_id", recipeId)
                        }
                    }
                    .decodeList<FavoriteRecipeDto>()
                    .isNotEmpty()
            } catch (e: Exception) {
                throw Exception("Failed to check favorite recipe: ${e.message}", e)
            }
        }
}
