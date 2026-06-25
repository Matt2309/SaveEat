package com.mattiamularoni.saveeat.features.pantry.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mattiamularoni.saveeat.features.pantry.domain.model.PantryAsset

@Entity(tableName = "pantry_item_assets")
data class PantryAssetEntity(
    @PrimaryKey
    @ColumnInfo(name = "category_key")
    val categoryKey: String,
    @ColumnInfo(name = "names")
    val names: Map<String, String>,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
)

fun PantryAssetEntity.toDomain() = PantryAsset(categoryKey, names, imageUrl)
