package com.mattiamularoni.saveeat.features.scan_receipt.domain.repository

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.features.pantry.domain.repository.PantryItem

interface ScanReceiptRepository {
    suspend fun analyzeReceiptImage(bitmap: Bitmap): List<PantryItem>
}