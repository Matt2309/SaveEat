package com.mattiamularoni.saveeat.features.scan_receipt.domain.model

/**
 * Prodotto estratto dallo scontrino dall'AI, prima di diventare un PantryItem.
 *
 * I prodotti deperibili ([isPerishable] == true) passano dal carosello di revisione
 * "Smart Expiry Date Review", mentre quelli a lunga conservazione vengono salvati
 * automaticamente nella dispensa.
 *
 * @property categoryKey una tra "FRIDGE" | "PANTRY" | "FREEZER"; mappa direttamente su
 *   PantryItem.category (valori non riconosciuti ricadono su PANTRY lato UI).
 * @property estimatedExpiryDays giorni di conservazione stimati dall'AI (es. 3 per pollo fresco).
 */
data class ParsedReceiptItem(
    val name: String,
    val categoryKey: String,
    val isPerishable: Boolean,
    val estimatedExpiryDays: Int,
    val quantity: Double,
    val unit: String
)
