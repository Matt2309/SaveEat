package com.mattiamularoni.saveeat.features.scan_receipt.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiReceiptItemDto(
    val name: String,
    @SerialName("category_key") val categoryKey: String = "generic_food",
    val category: String,
    val quantity: Double = 1.0,
    val unit: String = "pz",
    @SerialName("is_perishable") val isPerishable: Boolean = false,
    @SerialName("estimated_expiry_days") val estimatedExpiryDays: Int = 365,
)

@Serializable
data class GeminiReceiptResponseDto(
    @SerialName("store_name")
    val storeName: String,
    @SerialName("total_price")
    val totalPrice: Double,
    val items: List<GeminiReceiptItemDto>,
)
