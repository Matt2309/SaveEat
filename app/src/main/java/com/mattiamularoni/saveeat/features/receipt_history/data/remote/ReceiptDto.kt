package com.mattiamularoni.saveeat.features.receipt_history.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReceiptDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("store_name")
    val storeName: String,
    @SerialName("total_price")
    val totalPrice: Double,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("scanned_at")
    val scannedAt: String,
)
