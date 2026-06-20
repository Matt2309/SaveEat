package com.mattiamularoni.saveeat.ui.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestisce lo stato della preferenza "notifiche di scadenza", persistito in
 * SharedPreferences (sopravvive al riavvio dell'app). Stesso pattern di
 * ThemeController per le preferenze UI globali.
 */
class NotificationPreferencesController(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _expiryAlertsEnabled = MutableStateFlow(prefs.getBoolean(KEY_EXPIRY_ALERTS, true))
    val expiryAlertsEnabled: StateFlow<Boolean> = _expiryAlertsEnabled.asStateFlow()

    fun setExpiryAlertsEnabled(enabled: Boolean) {
        _expiryAlertsEnabled.value = enabled
        prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply()
    }

    private companion object {
        const val PREFS_NAME = "saveeat_settings"
        const val KEY_EXPIRY_ALERTS = "expiry_alerts_enabled"
    }
}
