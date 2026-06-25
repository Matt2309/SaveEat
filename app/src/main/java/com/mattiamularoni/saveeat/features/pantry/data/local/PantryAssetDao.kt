package com.mattiamularoni.saveeat.features.pantry.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryAssetDao {
    @Query("SELECT * FROM pantry_item_assets")
    fun observeAll(): Flow<List<PantryAssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(assets: List<PantryAssetEntity>)
}
