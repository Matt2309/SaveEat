package com.mattiamularoni.saveeat.features.pantry.data.repository

import com.mattiamularoni.saveeat.features.pantry.data.local.PantryAssetDao
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryAssetEntity
import com.mattiamularoni.saveeat.features.pantry.data.local.toDomain
import com.mattiamularoni.saveeat.features.pantry.data.remote.PantryAssetRemoteDataSource
import com.mattiamularoni.saveeat.features.pantry.domain.model.PantryAsset
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryAssetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PantryAssetRepositoryImpl(
    private val dao: PantryAssetDao,
    private val remote: PantryAssetRemoteDataSource,
) : PantryAssetRepository {
    override fun observeAssets(): Flow<Map<String, PantryAsset>> =
        dao.observeAll().map { list -> list.associate { it.categoryKey to it.toDomain() } }

    override suspend fun syncAssets() =
        withContext(Dispatchers.IO) {
            val entities =
                remote.getAllAssets().map { dto ->
                    PantryAssetEntity(dto.categoryKey, dto.names, dto.imageUrl)
                }
            dao.upsertAll(entities)
        }
}
