package com.mattiamularoni.saveeat.features.pantry.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO per la risposta Postgrest di Supabase.
 * Mapperà i campi JSON dello schema pantry_items.
 */
@Serializable
data class PantryItemDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("receipt_id")
    val receiptId: String?,
    val name: String,
    val category: String,
    @SerialName("is_placeholder")
    val isPlaceholder: Boolean = false,
    val status: String = "active",
    val quantity: Double? = null,
    val unit: String? = null,
    @SerialName("expiration_date")
    val expirationDate: String?,
    @SerialName("category_key")
    val categoryKey: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
