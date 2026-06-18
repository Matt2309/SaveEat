package com.mattiamularoni.saveeat.core.util

import java.time.Instant
import java.time.OffsetDateTime

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
    fun parseIso8601OrDefault(isoString: String?, fallback: Long = System.currentTimeMillis()): Long =
        parseToInstant(isoString)?.toEpochMilli() ?: fallback

    /**
     * Converte un timestamp ISO8601 in epoch millis, oppure null se [isoString]
     * è null o non parsabile.
     */
    fun parseIso8601OrNull(isoString: String?): Long? =
        parseToInstant(isoString)?.toEpochMilli()

    /**
     * Converte un timestamp epoch millis in stringa ISO8601 con suffisso Z (UTC).
     */
    fun formatToIso8601(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).toString()

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
