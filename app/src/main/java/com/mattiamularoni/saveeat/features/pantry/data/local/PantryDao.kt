package com.mattiamularoni.saveeat.features.pantry.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY name ASC")
    fun getPantryItems(): Flow<List<PantryEntity>>
}
