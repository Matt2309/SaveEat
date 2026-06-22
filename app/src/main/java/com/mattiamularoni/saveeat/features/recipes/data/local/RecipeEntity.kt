package com.mattiamularoni.saveeat.features.recipes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entità Room per le ricette in cache locale.
 *
 * Rappresenta una ricetta memorizzata nel database Room locale.
 * I dati vengono sincronizzati da Supabase e mantenuti in cache
 * per migliorare performance e supporto offline.
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val instructions: String,
    val ingredients: String,
    val prepTimeMinutes: Int,
    val tags: String,
    val createdAt: Long,
    val isVegetarian: Boolean = false,
    val estimatedWeightKg: Double = 0.0,
    val estimatedCostEuros: Double = 0.0
)
