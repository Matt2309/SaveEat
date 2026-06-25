package com.mattiamularoni.saveeat.features.auth.data.local

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "saveeat_biometric"
private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

/**
 * DataSource locale per la preferenza di accesso biometrico.
 *
 * Combina due tipi di persistenza:
 * - **Persistente** (SharedPreferences): flag `isBiometricEnabled` che sopravvive al riavvio dell'app.
 * - **In-memory** (campo di istanza): flag [biometricConfirmedThisSession] che indica se l'identità
 *   dell'utente è già stata confermata nella sessione app corrente (biometria riuscita o login
 *   esplicito con password). Viene resettato quando l'istanza viene distrutta (processo ucciso).
 *
 * Poiché questa classe è registrata come `single` in Koin, il campo [biometricConfirmedThisSession]
 * è condiviso tra tutte le istanze di ViewModel che la iniettano, garantendo coerenza anche
 * in presenza di multiple istanze di [AuthViewModel] (Activity-scoped vs NavBackStackEntry-scoped).
 */
class BiometricPreferenceDataSource(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Flag in-memory: diventa `true` quando l'utente ha confermato la propria identità
     * (biometria riuscita o login con password) nella sessione app corrente.
     *
     * Impedisce che i refresh automatici del token Supabase ri-richiedano la biometria
     * all'utente che ha già sbloccato l'app in questa sessione.
     *
     * Viene resettato a `false` al logout esplicito tramite [resetSessionConfirmation].
     */
    var biometricConfirmedThisSession: Boolean = false
        private set

    /** Segna la sessione corrente come confermata (biometria superata o login con password). */
    fun confirmSession() {
        biometricConfirmedThisSession = true
    }

    /** Resetta la conferma di sessione. Chiamare al logout per richiedere biometria al prossimo accesso. */
    fun resetSessionConfirmation() {
        biometricConfirmedThisSession = false
    }

    /** Legge la preferenza biometrica salvata localmente (persistente tra riavvii). */
    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    /** Salva la preferenza biometrica localmente in modo asincrono (apply). */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_BIOMETRIC_ENABLED, enabled) }
    }
}
