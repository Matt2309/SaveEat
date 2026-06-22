package com.mattiamularoni.saveeat.core.util

/**
 * Verifica se il nome di un ingrediente di ricetta corrisponde (fuzzy match)
 * al nome di un elemento della dispensa.
 *
 * Logica:
 * - Normalizza entrambi i nomi (lowercase, rimozione di eventuali parentesi)
 * - Match bidirezionale per sottostringa
 * - Fallback sulla prima parola (almeno 3 caratteri) per coprire varianti
 *   ("pomodori" vs "pomodoro pelato")
 *
 * Condivisa tra la UI (badge "In Dispensa") e la logica di deduzione
 * quantità, per garantire che mostrino sempre lo stesso risultato.
 *
 * @param ingredientName nome dell'ingrediente della ricetta
 * @param pantryName nome dell'elemento della dispensa
 * @return true se i due nomi sono considerati corrispondenti
 */
fun ingredientMatchesPantryName(ingredientName: String, pantryName: String): Boolean {
    val key = ingredientName.lowercase().substringBefore("(").trim()
    if (key.isBlank()) return false
    val pantry = pantryName.lowercase().trim()
    if (pantry.isBlank()) return false
    val first = key.split(" ").firstOrNull().orEmpty()
    return pantry.contains(key) || key.contains(pantry) || (first.length >= 3 && pantry.contains(first))
}
