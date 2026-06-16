package com.mattiamularoni.saveeat.features.pantry.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PantryTypeConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromNamesMap(map: Map<String, String>): String = json.encodeToString(map)

    @TypeConverter
    fun toNamesMap(value: String): Map<String, String> = json.decodeFromString(value)
}
