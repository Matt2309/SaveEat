package com.mattiamularoni.saveeat.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mattiamularoni.saveeat.features.home.data.local.HomeDao
import com.mattiamularoni.saveeat.features.home.data.local.HomeDashboardEntity
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryAssetDao
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryAssetEntity
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryDao
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryEntity
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryTypeConverters
import com.mattiamularoni.saveeat.features.recipes.data.local.FavoriteRecipeEntity
import com.mattiamularoni.saveeat.features.recipes.data.local.RecipeDao
import com.mattiamularoni.saveeat.features.recipes.data.local.RecipeEntity
import com.mattiamularoni.saveeat.features.stats.data.local.UserStatsDao
import com.mattiamularoni.saveeat.features.stats.data.local.UserStatsEntity

@Database(
    entities = [
        PantryEntity::class,
        PantryAssetEntity::class,
        RecipeEntity::class,
        FavoriteRecipeEntity::class,
        HomeDashboardEntity::class,
        UserStatsEntity::class
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(PantryTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pantryDao(): PantryDao
    abstract fun pantryAssetDao(): PantryAssetDao
    abstract fun recipeDao(): RecipeDao
    abstract fun homeDao(): HomeDao
    abstract fun userStatsDao(): UserStatsDao
}
