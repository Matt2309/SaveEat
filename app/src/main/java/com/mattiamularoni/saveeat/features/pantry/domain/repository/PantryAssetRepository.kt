package com.mattiamularoni.saveeat.features.pantry.domain.repository

import com.mattiamularoni.saveeat.features.pantry.domain.model.PantryAsset
import kotlinx.coroutines.flow.Flow

interface PantryAssetRepository {
    fun observeAssets(): Flow<Map<String, PantryAsset>>

    suspend fun syncAssets()
}
