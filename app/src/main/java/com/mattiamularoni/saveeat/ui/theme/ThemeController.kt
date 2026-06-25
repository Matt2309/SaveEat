package com.mattiamularoni.saveeat.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestisce lo stato della modalità scura, persistito in SharedPreferences
 * (sopravvive al riavvio dell'app).
 *
 * false = tema chiaro (default, coerente con i mockup brand)
 * true  = tema scuro
 */
class ThemeController(
    context: Context,
) {
    private val prefs =
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _darkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK, false))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
        prefs.edit().putBoolean(KEY_DARK, enabled).apply()
    }

    private companion object {
        const val PREFS_NAME = "saveeat_settings"
        const val KEY_DARK = "dark_mode"
    }
}
