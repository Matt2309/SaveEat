package com.mattiamularoni.saveeat.features.pantry.domain.repository

import kotlinx.coroutines.flow.Flow

interface PantryRepository {
    fun observePantryItems(): Flow<List<PantryItem>>
}

data class PantryItem(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String?,
    val expiresAt: Long?
)
