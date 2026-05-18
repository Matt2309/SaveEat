package com.mattiamularoni.saveeat.features.pantry.data.repository

import com.mattiamularoni.saveeat.features.pantry.data.local.PantryDao
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryEntity
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryItem
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PantryRepositoryImpl(
    private val pantryDao: PantryDao,
    private val supabaseClient: SupabaseClient
) : PantryRepository {

    override fun observePantryItems(): Flow<List<PantryItem>> {
        // TODO: Add Supabase fetch and Room caching logic here
        return pantryDao.getPantryItems()
            .map { entities -> entities.map { entity -> toDomain(entity) } }
    }

    private fun toDomain(entity: PantryEntity): PantryItem {
        return PantryItem(
            id = entity.id,
            name = entity.name,
            quantity = entity.quantity,
            unit = entity.unit,
            expiresAt = entity.expiresAt
        )
    }
}
