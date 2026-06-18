package com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.usecase

import android.graphics.Bitmap
import com.mattiamularoni.saveeat.features.scan_receipt.domain.model.ParsedReceiptItem
import com.mattiamularoni.saveeat.features.scan_receipt.domain.repository.ScanReceiptRepository

/**
 * Analizza la foto di uno scontrino con l'AI ed estrae i prodotti alimentari.
 *
 * Si limita all'analisi: il salvataggio (auto per i prodotti a lunga conservazione,
 * con revisione per i deperibili) è responsabilità del ViewModel.
 */
class AnalyzeReceiptUseCase(
    private val scanReceiptRepository: ScanReceiptRepository
) {
    /**
     * @param bitmap La foto dello scontrino
     * @return Result.success con i prodotti estratti, Result.failure in caso di errore
     */
    suspend operator fun invoke(bitmap: Bitmap): Result<List<ParsedReceiptItem>> {
        return try {
            val scannedItems = scanReceiptRepository.analyzeReceiptImage(bitmap)

            if (scannedItems.isEmpty()) {
                throw Exception("Nessun prodotto alimentare trovato nello scontrino.")
            }

            Result.success(scannedItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
