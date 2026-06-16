package com.mattiamularoni.saveeat.features.pantry.data.mapper

import com.mattiamularoni.saveeat.core.util.DateTimeUtils
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryEntity
import com.mattiamularoni.saveeat.features.pantry.data.remote.PantryItemDto

/**
 * Mapperà tra i layer:
 * - PantryItemDto (remote Supabase) ↔ PantryEntity (Room local)
 * - PantryEntity ↔ PantryItem (domain)
 */
object PantryMapper {

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
    fun dtoToEntity(dto: PantryItemDto): PantryEntity {
        return PantryEntity(
            id = dto.id,
            userId = dto.userId,
            receiptId = dto.receiptId,
            name = dto.name,
            category = dto.category,
            categoryKey = dto.categoryKey,
            isPlaceholder = dto.isPlaceholder,
            status = dto.status,
            quantity = dto.quantity ?: 1.0,
            unit = dto.unit,
            expirationDate = DateTimeUtils.parseIso8601OrNull(dto.expirationDate),
            createdAt = DateTimeUtils.parseIso8601OrDefault(dto.createdAt),
            updatedAt = DateTimeUtils.parseIso8601OrDefault(dto.updatedAt)
        )
    }

    /**
     * Converte un'entità Room in DTO remoto per upload/sync su Supabase.
     *
     * Responsabilità:
     * - Conversione timestamp Long → ISO8601 string
     *
     * @param entity entità Room
     * @return DTO pronto per l'invio a Postgrest
     */
    fun entityToDto(entity: PantryEntity): PantryItemDto {
        return PantryItemDto(
            id = entity.id,
            userId = entity.userId,
            receiptId = entity.receiptId,
            name = entity.name,
            category = entity.category,
            categoryKey = entity.categoryKey,
            isPlaceholder = entity.isPlaceholder,
            status = entity.status,
            quantity = entity.quantity,
            unit = entity.unit,
            expirationDate = entity.expirationDate?.let {
                DateTimeUtils.formatToIso8601(it)
            },
            createdAt = DateTimeUtils.formatToIso8601(entity.createdAt),
            updatedAt = DateTimeUtils.formatToIso8601(entity.updatedAt)
        )
    }

    /**
     * Converte una lista di DTO remoti in entità Room.
     * Utility per batch operations.
     *
     * @param dtos lista di DTO dalla risposta Postgrest
     * @return lista di entità Room
     */
    fun dtosToEntities(dtos: List<PantryItemDto>): List<PantryEntity> {
        return dtos.map { dtoToEntity(it) }
    }
}
