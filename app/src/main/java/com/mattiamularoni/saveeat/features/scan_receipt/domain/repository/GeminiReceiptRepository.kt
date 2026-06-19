package com.mattiamularoni.saveeat.features.scan_receipt.domain.repository

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryItem

data class ScannedReceiptData(
    val storeName: String,
    val totalPrice: Double,
    val items: List<PantryItem>
)

interface ScanReceiptRepository {
    suspend fun analyzeReceiptImage(bitmap: Bitmap): ScannedReceiptData
}