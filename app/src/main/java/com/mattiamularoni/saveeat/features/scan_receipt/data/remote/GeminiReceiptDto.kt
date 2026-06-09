package com.mattiamularoni.saveeat.features.scan_receipt.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class GeminiReceiptItemDto(
    val name: String,
    val category: String,
    val quantity: Double = 1.0,
    val unit: String = "pz"
)