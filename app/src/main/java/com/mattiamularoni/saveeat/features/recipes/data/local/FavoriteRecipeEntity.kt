package com.mattiamularoni.saveeat.features.recipes.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entità Room per le ricette preferite in cache locale.
 *
 * Rappresenta un elemento della relazione many-to-many tra utenti
 * e ricette preferite, memorizzato nel database Room locale.
 */
@Entity(
    tableName = "favorite_recipes",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FavoriteRecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val recipeId: String,
    val savedAt: Long
)
