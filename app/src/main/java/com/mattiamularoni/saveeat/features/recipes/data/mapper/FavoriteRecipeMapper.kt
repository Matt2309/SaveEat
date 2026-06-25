package com.mattiamularoni.saveeat.features.recipes.data.mapper

import com.mattiamularoni.saveeat.features.recipes.data.local.FavoriteRecipeEntity
import com.mattiamularoni.saveeat.features.recipes.data.remote.FavoriteRecipeDto
import com.mattiamularoni.saveeat.features.recipes.domain.repository.FavoriteRecipe
import java.time.Instant

/**
 * Mapper tra i layer:
 * - FavoriteRecipeDto (remote Supabase) ↔ FavoriteRecipeEntity (Room local) ↔ FavoriteRecipe (domain)
 *
 * Responsabilità:
 * - Conversione timestamp ISO8601 (DTO) ↔ Long ms (Entity)
 * - Mapping tra layer differenti
 */
object FavoriteRecipeMapper {
    /**
     * Converte un DTO remoto in entità Room locale.
     *
     * @param dto DTO dalla risposta Postgrest Supabase
     * @return entità Room pronta per il caching locale
     */
    fun dtoToEntity(dto: FavoriteRecipeDto): FavoriteRecipeEntity =
        FavoriteRecipeEntity(
            userId = dto.userId,
            recipeId = dto.recipeId,
            savedAt =
                try {
                    Instant.parse(dto.savedAt).toEpochMilli()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                },
        )

    /**
     * Converte un'entità Room in DTO remoto per upload/sync su Supabase.
     *
     * @param entity entità Room
     * @return DTO pronto per l'invio a Postgrest
     */
    fun entityToDto(entity: FavoriteRecipeEntity): FavoriteRecipeDto =
        FavoriteRecipeDto(
            userId = entity.userId,
            recipeId = entity.recipeId,
            savedAt = Instant.ofEpochMilli(entity.savedAt).toString(),
        )

    /**
     * Converte un'entità Room in domain model.
     *
     * @param entity entità Room
     * @return domain model
     */
    fun entityToDomain(entity: FavoriteRecipeEntity): FavoriteRecipe =
        FavoriteRecipe(
            userId = entity.userId,
            recipeId = entity.recipeId,
            savedAt = entity.savedAt,
        )

    /**
     * Converte un DTO remoto in domain model.
     *
     * @param dto DTO dalla risposta Postgrest Supabase
     * @return domain model
     */
    fun dtoToDomain(dto: FavoriteRecipeDto): FavoriteRecipe {
        val entity = dtoToEntity(dto)
        return entityToDomain(entity)
    }

    /**
     * Converte un domain model in entità Room.
     *
     * @param domain domain model
     * @return entità Room
     */
    fun domainToEntity(domain: FavoriteRecipe): FavoriteRecipeEntity =
        FavoriteRecipeEntity(
            userId = domain.userId,
            recipeId = domain.recipeId,
            savedAt = domain.savedAt,
        )

    /**
     * Converte una lista di DTO remoti in entità Room.
     *
     * @param dtos lista di DTO dalla risposta Postgrest
     * @return lista di entità Room
     */
    fun dtosToEntities(dtos: List<FavoriteRecipeDto>): List<FavoriteRecipeEntity> = dtos.map { dtoToEntity(it) }

    /**
     * Converte una lista di entità Room in domain models.
     *
     * @param entities lista di entità Room
     * @return lista di domain models
     */
    fun entitiesToDomain(entities: List<FavoriteRecipeEntity>): List<FavoriteRecipe> = entities.map { entityToDomain(it) }

    /**
     * Converte una lista di DTO remoti in domain models.
     *
     * @param dtos lista di DTO dalla risposta Postgrest
     * @return lista di domain models
     */
    fun dtosToDomain(dtos: List<FavoriteRecipeDto>): List<FavoriteRecipe> = dtos.map { dtoToDomain(it) }
}
