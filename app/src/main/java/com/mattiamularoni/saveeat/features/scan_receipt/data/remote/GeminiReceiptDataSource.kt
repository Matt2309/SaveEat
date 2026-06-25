package com.mattiamularoni.saveeat.features.scan_receipt.data.remote

import android.graphics.Bitmap

/**
 * Contratto per il servizio di lettura scontrini tramite AI.
 */
interface GeminiReceiptDataSource {
    /**
     * Analizza l'immagine di uno scontrino e restituisce una stringa JSON
     * contenente la lista dei prodotti identificati.
     */
    suspend fun analyzeReceipt(bitmap: Bitmap): String
}
