package com.mattiamularoni.saveeat.features.home.data.mapper

import com.mattiamularoni.saveeat.core.util.DateTimeUtils
import com.mattiamularoni.saveeat.features.home.data.local.HomeDashboardEntity
import com.mattiamularoni.saveeat.features.home.data.remote.HomeDashboardDto
import kotlinx.serialization.json.Json

/**
 * Mapper tra i layer per il modulo Home:
 * - HomeDashboardDto (remote Supabase) ↔ HomeDashboardEntity (Room local)
 * - HomeDashboardEntity ↔ HomeDashboard (domain)
 *
 * Responsabilità:
 * - Serializzazione/deserializzazione JSON per persistenza Room
 * - Conversione timestamp ISO8601 → Long (millisecondi) e viceversa
 * - Normalizzazione dati tra layer
 */
object HomeMapper {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Converte un DTO remoto in entità Room locale.
     *
     * Responsabilità:
     * - Serializzazione nested objects (ExpiringItemDto, etc.) in JSON string
     * - Timestamp setting: createdAt = now, lastSyncedAt = now
     *
     * @param dto DTO dalla risposta Postgrest Supabase
     * @param userId UUID dell'utente
     * @return entità Room pronta per il caching locale
     */
    fun dtoToEntity(
        dto: HomeDashboardDto,
        userId: String,
    ): HomeDashboardEntity {
        val now = System.currentTimeMillis()

        return HomeDashboardEntity(
            userId = userId,
            expiringItemsJson = json.encodeToString(dto.expiringItems),
            topLeaderboardJson = json.encodeToString(dto.topLeaderboard),
            suggestedRecipesJson = json.encodeToString(dto.suggestedRecipes),
            userStatsJson = json.encodeToString(dto.userStats),
            userProfileJson = json.encodeToString(dto.userProfile),
            lastSyncedAt = now,
            createdAt = now,
        )
    }

    /**
     * Converte un'entità Room in DTO remoto per aggiornamenti/sync su Supabase.
     *
     * Responsabilità:
     * - Deserializzazione JSON string in domain objects
     * - Conversione timestamp Long → ISO8601 string
     *
     * @param entity entità Room
     * @return DTO pronto per l'invio a Postgrest (se necessario)
     */
    fun entityToDto(entity: HomeDashboardEntity): HomeDashboardDto =
        try {
            HomeDashboardDto(
                userId = entity.userId,
                expiringItems = json.decodeFromString(entity.expiringItemsJson),
                topLeaderboard = json.decodeFromString(entity.topLeaderboardJson),
                suggestedRecipes = json.decodeFromString(entity.suggestedRecipesJson),
                userStats = json.decodeFromString(entity.userStatsJson),
                userProfile = json.decodeFromString(entity.userProfileJson),
                lastSyncedAt = entity.lastSyncedAt.toString(),
            )
        } catch (e: Exception) {
            throw Exception("Failed to deserialize Home dashboard entity: ${e.message}", e)
        }

    /**
     * Converte DTO in domain model (HomeDashboard).
     *
     * @param dto DTO remoto
     * @return domain model com.mattiamularoni.saveeat.features.home.domain.repository.HomeDashboard
     */
    fun dtoToDomain(dto: HomeDashboardDto): com.mattiamularoni.saveeat.features.home.domain.repository.HomeDashboard =
        com.mattiamularoni.saveeat.features.home.domain.repository.HomeDashboard(
            userId = dto.userId,
            expiringItems =
                dto.expiringItems.map { item ->
                    com.mattiamularoni.saveeat.features.home.domain.repository.ExpiringItem(
                        id = item.id,
                        name = item.name,
                        category = item.category,
                        categoryKey = item.categoryKey,
                        expirationDate = DateTimeUtils.parseIso8601OrDefault(item.expirationDate),
                        quantity = item.quantity,
                        unit = item.unit,
                    )
                },
            topLeaderboardUsers =
                dto.topLeaderboard.map { user ->
                    com.mattiamularoni.saveeat.features.home.domain.repository.LeaderboardUser(
                        id = user.id,
                        email = user.email,
                        displayName = user.displayName,
                        avatarUrl = user.avatarUrl,
                        ecoPoints = user.ecoPoints,
                    )
                },
            suggestedRecipes =
                dto.suggestedRecipes.map { recipe ->
                    com.mattiamularoni.saveeat.features.home.domain.repository.SuggestedRecipe(
                        id = recipe.id,
                        title = recipe.title,
                        tags = recipe.tags?.split(",")?.map { it.trim() } ?: emptyList(),
                        prepTimeMinutes = recipe.prepTimeMinutes,
                        matchingIngredients = recipe.matchingIngredients,
                        imageUrl = recipe.imageUrl,
                    )
                },
            userStats =
                com.mattiamularoni.saveeat.features.home.domain.repository.UserStats(
                    totalItems = dto.userStats.totalItems,
                    expiringCount = dto.userStats.expiringCount,
                    activePlaceholders = dto.userStats.activePlaceholders,
                    ecoPoints = dto.userStats.ecoPoints,
                ),
            userProfile =
                com.mattiamularoni.saveeat.features.home.domain.repository.UserProfile(
                    id = dto.userProfile.id,
                    displayName = dto.userProfile.displayName,
                    avatarUrl = dto.userProfile.avatarUrl,
                    rankPosition = dto.userProfile.rankPosition,
                ),
            lastSyncedAt = DateTimeUtils.parseIso8601OrDefault(dto.lastSyncedAt),
        )

    /**
     * Converte entità Room in domain model.
     *
     * @param entity entità Room
     * @return domain model
     */
    fun entityToDomain(entity: HomeDashboardEntity): com.mattiamularoni.saveeat.features.home.domain.repository.HomeDashboard {
        val dto = entityToDto(entity)
        return dtoToDomain(dto)
    }

    /**
     * Converte domain model in entità Room.
     *
     * @param domain domain model
     * @return entità Room
     */
    fun domainToEntity(domain: com.mattiamularoni.saveeat.features.home.domain.repository.HomeDashboard): HomeDashboardEntity {
        val dto =
            HomeDashboardDto(
                userId = domain.userId,
                expiringItems =
                    domain.expiringItems.map { item ->
                        com.mattiamularoni.saveeat.features.home.data.remote.ExpiringItemDto(
                            id = item.id,
                            name = item.name,
                            category = item.category,
                            categoryKey = item.categoryKey,
                            expirationDate = DateTimeUtils.formatToIso8601(item.expirationDate),
                            quantity = item.quantity,
                            unit = item.unit,
                        )
                    },
                topLeaderboard =
                    domain.topLeaderboardUsers.map { user ->
                        com.mattiamularoni.saveeat.features.home.data.remote.LeaderboardUserDto(
                            id = user.id,
                            email = user.email,
                            displayName = user.displayName,
                            avatarUrl = user.avatarUrl,
                            ecoPoints = user.ecoPoints,
                        )
                    },
                suggestedRecipes =
                    domain.suggestedRecipes.map { recipe ->
                        com.mattiamularoni.saveeat.features.home.data.remote.SuggestedRecipeDto(
                            id = recipe.id,
                            title = recipe.title,
                            tags = recipe.tags.joinToString(","),
                            prepTimeMinutes = recipe.prepTimeMinutes,
                            matchingIngredients = recipe.matchingIngredients,
                            imageUrl = recipe.imageUrl,
                        )
                    },
                userStats =
                    com.mattiamularoni.saveeat.features.home.data.remote.UserStatsDto(
                        totalItems = domain.userStats.totalItems,
                        expiringCount = domain.userStats.expiringCount,
                        activePlaceholders = domain.userStats.activePlaceholders,
                        ecoPoints = domain.userStats.ecoPoints,
                    ),
                userProfile =
                    com.mattiamularoni.saveeat.features.home.data.remote.UserProfileDto(
                        id = domain.userProfile.id,
                        displayName = domain.userProfile.displayName,
                        avatarUrl = domain.userProfile.avatarUrl,
                        rankPosition = domain.userProfile.rankPosition,
                    ),
                lastSyncedAt = DateTimeUtils.formatToIso8601(domain.lastSyncedAt),
            )
        return dtoToEntity(dto, domain.userId)
    }
}
