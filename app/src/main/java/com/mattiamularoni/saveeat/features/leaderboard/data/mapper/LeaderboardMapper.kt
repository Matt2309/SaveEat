package com.mattiamularoni.saveeat.features.leaderboard.data.mapper

import com.mattiamularoni.saveeat.features.leaderboard.data.remote.LeaderboardUserDto
import com.mattiamularoni.saveeat.features.leaderboard.domain.repository.LeaderboardUser

/**
 * Mapper tra i layer:
 * - LeaderboardUserDto (remote Supabase) ↔ LeaderboardUser (domain)
 */
object LeaderboardMapper {
    /**
     * Converte un DTO remoto in domain model.
     *
     * @param dto DTO dalla risposta Postgrest Supabase
     * @return domain model pronto per business logic
     */
    fun dtoToDomain(dto: LeaderboardUserDto): LeaderboardUser =
        LeaderboardUser(
            id = dto.id,
            email = dto.email,
            displayName = dto.displayName,
            avatarUrl = dto.avatarUrl,
            ecoPoints = dto.ecoPoints,
            rank = null,
        )

    /**
     * Converte una lista di DTO remoti in domain models.
     * Utility per batch operations.
     *
     * @param dtos lista di DTO dalla risposta Postgrest
     * @return lista di domain models
     */
    fun dtosToDomain(dtos: List<LeaderboardUserDto>): List<LeaderboardUser> = dtos.map { dtoToDomain(it) }

    /**
     * Converte un domain model in DTO remoto per upload/sync su Supabase.
     *
     * @param domain domain model
     * @return DTO pronto per l'invio a Postgrest
     */
    fun domainToDto(domain: LeaderboardUser): LeaderboardUserDto =
        LeaderboardUserDto(
            id = domain.id,
            email = domain.email,
            displayName = domain.displayName,
            avatarUrl = domain.avatarUrl,
            ecoPoints = domain.ecoPoints,
        )
}
