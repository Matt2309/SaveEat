package com.mattiamularoni.saveeat.core.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Conversioni condivise tra timestamp ISO8601 (remote) e epoch millis (locale).
 */
object DateTimeUtils {
    /**
     * Converte un timestamp ISO8601 in epoch millis.
     *
     * Supabase restituisce offset notation ("2026-06-17T22:00:00+00:00") invece di
     * "Z" notation — Instant.parse() non la riconosce, quindi si fa fallback su
     * OffsetDateTime.parse() che gestisce entrambi i formati.
     *
     * @param isoString timestamp ISO8601, oppure null
     * @param fallback valore restituito se [isoString] è null o non parsabile
     */
    fun parseIso8601OrDefault(
        isoString: String?,
        fallback: Long = System.currentTimeMillis(),
    ): Long = parseToInstant(isoString)?.toEpochMilli() ?: fallback

    /**
     * Converte un timestamp ISO8601 in epoch millis, oppure null se [isoString]
     * è null o non parsabile.
     */
    fun parseIso8601OrNull(isoString: String?): Long? = parseToInstant(isoString)?.toEpochMilli()

    /**
     * Converte un timestamp epoch millis in stringa ISO8601 con suffisso Z (UTC).
     */
    fun formatToIso8601(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).toString()

    /**
     * Formatta un epoch millis come data leggibile in italiano, es. "12 Ottobre 2023 • 14:30".
     *
     * Il nome del mese va capitalizzato manualmente: Locale.ITALIAN restituisce
     * i nomi dei mesi in minuscolo ("ottobre"), quindi replaceFirstChar sull'intera
     * stringa non basta (il primo carattere è la cifra del giorno).
     */
    fun formatReceiptDisplayDate(epochMillis: Long): String {
        val dateTime = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
        val month =
            dateTime.month
                .getDisplayName(java.time.format.TextStyle.FULL, Locale.ITALIAN)
                .replaceFirstChar { it.uppercase() }
        val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        return "${dateTime.dayOfMonth} $month ${dateTime.year} • $time"
    }

    private fun parseToInstant(isoString: String?): Instant? {
        if (isoString == null) return null
        return try {
            Instant.parse(isoString)
        } catch (_: Exception) {
            try {
                OffsetDateTime.parse(isoString).toInstant()
            } catch (_: Exception) {
                null
            }
        }
    }
}
