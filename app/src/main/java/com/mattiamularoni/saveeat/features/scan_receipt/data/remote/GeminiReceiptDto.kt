package com.mattiamularoni.saveeat.features.scan_receipt.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiReceiptItemDto(
    val name: String,
    val category: String,
    val quantity: Double = 1.0,
    val unit: String = "pz"
)

@Serializable
data class GeminiReceiptResponseDto(
    @SerialName("store_name")
    val storeName: String,
    @SerialName("total_price")
    val totalPrice: Double,
    val items: List<GeminiReceiptItemDto>
)