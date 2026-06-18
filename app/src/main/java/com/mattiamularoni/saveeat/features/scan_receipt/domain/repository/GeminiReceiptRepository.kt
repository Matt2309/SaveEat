package com.mattiamularoni.saveeat.features.scan_receipt.domain.repository

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.features.scan_receipt.domain.model.ParsedReceiptItem

interface ScanReceiptRepository {
    suspend fun analyzeReceiptImage(bitmap: Bitmap): List<ParsedReceiptItem>
}
