package com.mattiamularoni.saveeat.features.receipt_history.domain.repository

import android.graphics.Bitmap

data class Receipt(
    val id: String,
    val userId: String,
    val storeName: String,
    val totalPrice: Double,
    val imageUrl: String,
    val scannedAt: Long,
)

interface ReceiptRepository {
    suspend fun uploadReceiptImage(bitmap: Bitmap): String

    suspend fun insertReceipt(
        storeName: String,
        totalPrice: Double,
        imageUrl: String,
    ): Receipt

    suspend fun getReceipts(): List<Receipt>
}
