package com.mattiamularoni.saveeat.features.recipes.data.mapper

import com.mattiamularoni.saveeat.core.util.DateTimeUtils
import com.mattiamularoni.saveeat.features.recipes.data.local.RecipeEntity
import com.mattiamularoni.saveeat.features.recipes.data.remote.RecipeDto
import com.mattiamularoni.saveeat.features.recipes.domain.repository.Recipe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Mapper tra i layer:
 * - RecipeDto (remote Supabase) ↔ RecipeEntity (Room local) ↔ Recipe (domain)
 *
 * Responsabilità:
 * - Conversione timestamp ISO8601 (DTO) ↔ Long ms (Entity)
 * - Parsing ingredienti JSON
 * - Parsing tags da stringa a lista
 */
object RecipeMapper {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Data class interno per il parsing JSON degli ingredienti.
     * Utilizzato da kotlinx.serialization per deserializzare da JSON.
     */
    @Serializable
    private data class IngredientJson(
        val name: String,
        val amount: Double = 1.0,
        val unit: String = "qb",
    )

    /**
     * Converte un DTO remoto in entità Room locale.
     *
     * Responsabilità:
     * - Conversione timestamp ISO8601 → Long (millisecondi)
     * - Normalizzazione dati
     *
     * @param dto DTO dalla risposta Postgrest Supabase
     * @return entità Room pronta per il caching locale
     */
    fun dtoToEntity(dto: RecipeDto): RecipeEntity =
        RecipeEntity(
            id = dto.id,
            title = dto.title,
            instructions = dto.instructions,
            ingredients = dto.ingredients,
            prepTimeMinutes = dto.prepTimeMinutes,
            tags = dto.tags,
            createdAt = DateTimeUtils.parseIso8601OrDefault(dto.createdAt),
            isVegetarian = dto.isVegetarian,
            estimatedWeightKg = dto.estimatedWeightKg,
            estimatedCostEuros = dto.estimatedCostEuros,
            imageUrl = dto.imageUrl,
        )

    /**
     * Converte un'entità Room in DTO remoto per upload/sync su Supabase.
     *
     * Responsabilità:
     * - Conversione timestamp Long → ISO8601 string
     *
     * @param entity entità Room
     * @return DTO pronto per l'invio a Postgrest
     */
    fun entityToDto(entity: RecipeEntity): RecipeDto =
        RecipeDto(
            id = entity.id,
            title = entity.title,
            instructions = entity.instructions,
            ingredients = entity.ingredients,
            prepTimeMinutes = entity.prepTimeMinutes,
            tags = entity.tags,
            createdAt = DateTimeUtils.formatToIso8601(entity.createdAt),
            isVegetarian = entity.isVegetarian,
            estimatedWeightKg = entity.estimatedWeightKg,
            estimatedCostEuros = entity.estimatedCostEuros,
            imageUrl = entity.imageUrl,
        )

    /**
     * Converte un'entità Room in domain model.
     *
     * Responsabilità:
     * - Parsing ingredienti da stringa JSON a lista
     * - Parsing tags da stringa a lista
     * - Conversione timestamp per logica business
     *
     * @param entity entità Room
     * @return domain model pronto per business logic
     */
    fun entityToDomain(entity: RecipeEntity): Recipe =
        Recipe(
            id = entity.id,
            title = entity.title,
            instructions = entity.instructions,
            ingredients = parseIngredients(entity.ingredients),
            prepTimeMinutes = entity.prepTimeMinutes,
            tags = parseTags(entity.tags),
            createdAt = entity.createdAt,
            isVegetarian = entity.isVegetarian,
            estimatedWeightKg = entity.estimatedWeightKg,
            estimatedCostEuros = entity.estimatedCostEuros,
            imageUrl = entity.imageUrl,
        )

    /**
     * Converte un DTO remoto in domain model.
     *
     * @param dto DTO dalla risposta Postgrest Supabase
     * @return domain model
     */
    fun dtoToDomain(dto: RecipeDto): Recipe {
        val entity = dtoToEntity(dto)
        return entityToDomain(entity)
    }

    /**
     * Converte un domain model in entità Room.
     *
     * @param domain domain model
     * @return entità Room
     */
    fun domainToEntity(domain: Recipe): RecipeEntity =
        RecipeEntity(
            id = domain.id,
            title = domain.title,
            instructions = domain.instructions,
            ingredients = serializeIngredients(domain.ingredients),
            prepTimeMinutes = domain.prepTimeMinutes,
            tags = serializeTags(domain.tags),
            createdAt = domain.createdAt,
            isVegetarian = domain.isVegetarian,
            estimatedWeightKg = domain.estimatedWeightKg,
            estimatedCostEuros = domain.estimatedCostEuros,
            imageUrl = domain.imageUrl,
        )

    /**
     * Converte una lista di DTO remoti in entità Room.
     * Utility per batch operations.
     *
     * @param dtos lista di DTO dalla risposta Postgrest
     * @return lista di entità Room
     */
    fun dtosToEntities(dtos: List<RecipeDto>): List<RecipeEntity> = dtos.map { dtoToEntity(it) }

    /**
     * Converte una lista di entità Room in domain models.
     * Utility per batch operations.
     *
     * @param entities lista di entità Room
     * @return lista di domain models
     */
    fun entitiesToDomain(entities: List<RecipeEntity>): List<Recipe> = entities.map { entityToDomain(it) }

    /**
     * Converte una lista di DTO remoti in domain models.
     * Utility per batch operations.
     *
     * @param dtos lista di DTO dalla risposta Postgrest
     * @return lista di domain models
     */
    fun dtosToDomain(dtos: List<RecipeDto>): List<Recipe> = dtos.map { dtoToDomain(it) }

    // ===== PRIVATE HELPERS =====

    /**
     * Parsa ingredienti da stringa JSON.
     *
     * Formato atteso: JSON array di ingredienti con nome/quantità/unità.
     * Es: [{"name":"pollo","amount":500,"unit":"g"}]
     *
     * @param ingredientsJson stringa JSON degli ingredienti
     * @return lista di ingredienti parsati
     */
    private fun parseIngredients(ingredientsJson: String): List<Recipe.Ingredient> {
        return try {
            if (ingredientsJson.isBlank() || ingredientsJson == "[]") {
                return emptyList()
            }

            // Parse JSON array using kotlinx.serialization
            val parsedIngredients = json.decodeFromString<List<IngredientJson>>(ingredientsJson)

            parsedIngredients.map { ingredient ->
                Recipe.Ingredient(
                    name = ingredient.name,
                    amount = ingredient.amount,
                    unit = ingredient.unit,
                )
            }
        } catch (e: Exception) {
            // Return empty list on JSON parse error, but log it so failures are visible
            android.util.Log.e("RecipeMapper", "Failed to parse ingredients JSON: $ingredientsJson", e)
            emptyList()
        }
    }

    /**
     * Parsa tag da stringa separata da virgole.
     *
     * Formato: "tag1,tag2,tag3" oppure JSON array
     *
     * @param tagsString stringa dei tag
     * @return lista di tag
     */
    private fun parseTags(tagsString: String): List<String> {
        return try {
            if (tagsString.isEmpty()) return emptyList()
            tagsString
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Serializza ingredienti a stringa JSON.
     *
     * @param ingredients lista di ingredienti
     * @return stringa JSON
     */
    private fun serializeIngredients(ingredients: List<Recipe.Ingredient>): String {
        return try {
            if (ingredients.isEmpty()) {
                return "[]"
            }

            val json = Json
            val ingredientsJson =
                ingredients.map { ingredient ->
                    IngredientJson(
                        name = ingredient.name,
                        amount = ingredient.amount,
                        unit = ingredient.unit,
                    )
                }

            json.encodeToString(ingredientsJson)
        } catch (e: Exception) {
            // Silently return empty array on JSON encode error
            "[]"
        }
    }

    /**
     * Serializza tag a stringa separata da virgole.
     *
     * @param tags lista di tag
     * @return stringa comma-separated
     */
    private fun serializeTags(tags: List<String>): String = tags.joinToString(",")
}
