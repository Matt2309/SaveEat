package com.mattiamularoni.saveeat.features.pantry.data.mapper

import com.mattiamularoni.saveeat.features.pantry.data.local.PantryEntity
import com.mattiamularoni.saveeat.features.pantry.data.remote.PantryItemDto
import java.time.Instant

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
            isPlaceholder = dto.isPlaceholder,
            status = dto.status,
            quantity = dto.quantity ?: 1.0,
            unit = dto.unit,
            expirationDate = dto.expirationDate?.let { dateString ->
                try {
                    Instant.parse(dateString).toEpochMilli()
                } catch (e: Exception) {
                    null
                }
            },
            createdAt = try {
                Instant.parse(dto.createdAt).toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            },
            updatedAt = try {
                Instant.parse(dto.updatedAt).toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
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
            isPlaceholder = entity.isPlaceholder,
            status = entity.status,
            quantity = entity.quantity,
            unit = entity.unit,
            expirationDate = entity.expirationDate?.let {
                Instant.ofEpochMilli(it).toString()
            },
            createdAt = Instant.ofEpochMilli(entity.createdAt).toString(),
            updatedAt = Instant.ofEpochMilli(entity.updatedAt).toString()
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
