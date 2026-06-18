package com.mattiamularoni.saveeat.core.data.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Gestisce la foto profilo SCELTA IN LOCALE dall'utente (task: "Implementare solo in locale").
 *
 * - Copia l'immagine selezionata nello storage interno dell'app (così persiste al riavvio).
 * - Espone il percorso del file come StateFlow, osservato dall'avatar in tutta l'app.
 *
 * Se non è stata impostata nessuna foto locale, l'avatar usa come fallback la foto di Google
 * (vedi SessionProvider.getAvatarUrl()).
 */
class ProfilePhotoController(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _localPhotoPath = MutableStateFlow(prefs.getString(KEY_PHOTO_PATH, null))
    val localPhotoPath: StateFlow<String?> = _localPhotoPath.asStateFlow()

    /**
     * Copia l'immagine indicata da [uri] nello storage interno e la imposta come foto profilo.
     */
    fun setLocalPhoto(uri: Uri) {
        runCatching {
            val file = File(appContext.filesDir, "profile_${System.currentTimeMillis()}.jpg")
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            // rimuove le foto profilo precedenti per non accumulare file
            appContext.filesDir.listFiles { f -> f.name.startsWith("profile_") && f != file }
                ?.forEach { it.delete() }

            _localPhotoPath.value = file.absolutePath
            prefs.edit().putString(KEY_PHOTO_PATH, file.absolutePath).apply()
        }
    }

    /** Rimuove la foto locale (torna alla foto di Google / icona di default). */
    fun clearLocalPhoto() {
        _localPhotoPath.value = null
        prefs.edit().remove(KEY_PHOTO_PATH).apply()
    }

    private companion object {
        const val PREFS_NAME = "saveeat_settings"
        const val KEY_PHOTO_PATH = "profile_photo_path"
    }
}
