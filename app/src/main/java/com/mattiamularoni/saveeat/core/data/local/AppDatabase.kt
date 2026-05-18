package com.mattiamularoni.saveeat.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryDao
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryEntity

@Database(
    entities = [PantryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pantryDao(): PantryDao
}
