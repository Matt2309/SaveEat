package com.mattiamularoni.saveeat.features.stats.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entità Room per la cache locale delle statistiche di risparmio dell'utente.
 *
 * Relazione 1-a-1 con l'utente autenticato (PK = user_id).
 */
@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "total_kg_saved")
    val totalKgSaved: Double = 0.0,
    @ColumnInfo(name = "total_euros_saved")
    val totalEurosSaved: Double = 0.0,
    @ColumnInfo(name = "total_eco_points")
    val totalEcoPoints: Int = 0,
)
