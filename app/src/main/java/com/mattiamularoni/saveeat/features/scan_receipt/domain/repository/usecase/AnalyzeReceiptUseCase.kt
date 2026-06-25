package com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.usecase

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.features.receipt_history.domain.repository.ReceiptRepository
import com.mattiamularoni.saveeat.features.scan_receipt.domain.model.ParsedReceiptItem
import com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.ScanReceiptRepository

data class AnalyzedReceipt(
    val receiptId: String,
    val items: List<ParsedReceiptItem>,
)

/**
 * Analizza la foto di uno scontrino con l'AI, carica la foto e registra lo scontrino.
 *
 * Si limita all'analisi e alla registrazione dello scontrino: il salvataggio dei singoli
 * prodotti in dispensa (auto per quelli a lunga conservazione, con revisione per i deperibili)
 * è responsabilità del ViewModel.
 */
class AnalyzeReceiptUseCase(
    private val scanReceiptRepository: ScanReceiptRepository,
    private val receiptRepository: ReceiptRepository,
) {
    /**
     * @param bitmap La foto dello scontrino
     * @return Result.success con lo scontrino registrato e i prodotti estratti, Result.failure in caso di errore
     */
    suspend operator fun invoke(bitmap: Bitmap): Result<AnalyzedReceipt> =
        try {
            // 1. Analyze image with IA (store name, total price, items)
            val scanned = scanReceiptRepository.analyzeReceiptImage(bitmap)

            if (scanned.items.isEmpty()) {
                throw Exception("Nessun prodotto alimentare trovato nello scontrino.")
            }

            // 2. Upload receipt photo to Supabase Storage
            val imageUrl = receiptRepository.uploadReceiptImage(bitmap)

            // 3. Insert the receipt row (store name, total price, image url)
            val receipt =
                receiptRepository.insertReceipt(
                    storeName = scanned.storeName,
                    totalPrice = scanned.totalPrice,
                    imageUrl = imageUrl,
                )

            Result.success(AnalyzedReceipt(receiptId = receipt.id, items = scanned.items))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
