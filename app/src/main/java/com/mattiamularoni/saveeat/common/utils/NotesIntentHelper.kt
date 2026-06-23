package com.mattiamularoni.saveeat.common.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.mattiamularoni.saveeat.features.shopping_list.domain.model.ShoppingListItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Apre l'app Note del telefono con la lista della spesa come contenuto,
 * provando in cascata Samsung Notes, Google Keep e infine un chooser generico.
 *
 * Usa `setPackage` + `startActivity` + catch(ActivityNotFoundException) invece
 * di interrogare il PackageManager (resolveActivity/queryIntentActivities):
 * questo evita di dover dichiarare le app target in `<queries>` nel manifest,
 * dato che le regole di package-visibility di Android 11+ riguardano solo le query.
 */
object NotesIntentHelper {

    private const val SAMSUNG_NOTES_PACKAGE = "com.samsung.android.app.notes"
    private const val GOOGLE_KEEP_PACKAGE = "com.google.android.keep"

    fun openNotesWithShoppingList(context: Context, items: List<ShoppingListItem>) {
        val content = buildNoteContent(items)

        if (trySend(context, content, SAMSUNG_NOTES_PACKAGE)) return
        if (trySend(context, content, GOOGLE_KEEP_PACKAGE)) return
        trySendChooser(context, content)
    }

    private fun buildNoteContent(items: List<ShoppingListItem>): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        return "🛒 Lista della spesa - SaveEat\n\n" +
            items.joinToString("\n") { "• ${it.name}" } +
            "\n\nAggiornata il: $timestamp"
    }

    private fun trySend(context: Context, content: String, packageName: String): Boolean =
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
                setPackage(packageName)
            }
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }

    private fun trySendChooser(context: Context, content: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
            }
            context.startActivity(Intent.createChooser(intent, "Apri con"))
        } catch (e: ActivityNotFoundException) {
            // Nessuna app gestisce ACTION_SEND text/plain: no-op.
        }
    }
}
