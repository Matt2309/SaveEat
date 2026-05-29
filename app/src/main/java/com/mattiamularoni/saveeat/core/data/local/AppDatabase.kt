package com.mattiamularoni.saveeat.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryDao
import com.mattiamularoni.saveeat.features.pantry.data.local.PantryEntity
import com.mattiamularoni.saveeat.features.recipes.data.local.FavoriteRecipeEntity
import com.mattiamularoni.saveeat.features.recipes.data.local.RecipeDao
import com.mattiamularoni.saveeat.features.recipes.data.local.RecipeEntity

@Database(
    entities = [
        PantryEntity::class,
        RecipeEntity::class,
        FavoriteRecipeEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pantryDao(): PantryDao
    abstract fun recipeDao(): RecipeDao
}

