package com.mattiamularoni.saveeat.features.pantry.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// TODO (Architect): Confirm pantry_items columns and types with backend/domain contracts once finalized.
@Entity(tableName = "pantry_items")
data class PantryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "quantity")
    val quantity: Double,
    @ColumnInfo(name = "unit")
    val unit: String?,
    @ColumnInfo(name = "expires_at")
    val expiresAt: Long?
)
