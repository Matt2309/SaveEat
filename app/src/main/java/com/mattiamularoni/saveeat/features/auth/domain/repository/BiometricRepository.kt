package com.mattiamularoni.saveeat.features.auth.domain.repository

import com.mattiamularoni.saveeat.features.auth.domain.model.BiometricAvailabilityStatus

/**
 * Contratto domain per la gestione dell'autenticazione biometrica.
 *
 * Astrae tre responsabilità:
 * - verifica delle capacità hardware del dispositivo
 * - gestione della preferenza locale dell'utente (persistente)
 * - stato di conferma della sessione corrente (in-memory, condiviso tra ViewModel)
 * - sincronizzazione della preferenza con il backend Supabase
 *
 * ### Nota sull'istanza condivisa
 * Questa interfaccia è implementata da un `single` Koin. Il flag [isBiometricConfirmedThisSession]
 * è quindi condiviso tra tutte le istanze di ViewModel che la iniettano, garantendo che la
 * conferma di identità da qualsiasi ViewModel sia visibile agli altri.
 */
interface BiometricRepository {

    /**
     * Verifica se il dispositivo supporta la biometria e se l'utente
     * ha registrato almeno una credenziale biometrica valida.
     *
     * @return lo stato di disponibilità hardware della biometria.
     */
    fun getBiometricAvailabilityStatus(): BiometricAvailabilityStatus

    /**
     * Legge se l'utente ha abilitato il login biometrico su questo dispositivo.
     *
     * @return `true` se la preferenza è stata salvata come abilitata.
     */
    fun isBiometricLoginEnabled(): Boolean

    /**
     * Verifica se esiste una sessione Supabase autenticata e attiva.
     *
     * @return `true` se la sessione è autenticata.
     */
    fun isSessionAuthenticated(): Boolean

    /**
     * Verifica se l'identità dell'utente è già stata confermata nella sessione app corrente.
     *
     * @return `true` se biometria o login con password sono stati completati con successo.
     */
    fun isBiometricConfirmedThisSession(): Boolean

    /**
     * Segna la sessione corrente come confermata (identità verificata).
     * Da chiamare dopo biometria riuscita o login con password riuscito.
     */
    fun confirmSession()

    /**
     * Resetta la conferma di sessione.
     * Da chiamare al logout per richiedere la verifica al prossimo accesso.
     */
    fun resetSessionConfirmation()

    /**
     * Abilita il login biometrico per questo utente su questo dispositivo.
     * Salva la preferenza localmente e sincronizza con Supabase.
     */
    suspend fun enableBiometricLogin()

    /**
     * Disabilita il login biometrico per questo utente su questo dispositivo.
     * Salva la preferenza localmente e sincronizza con Supabase.
     */
    suspend fun disableBiometricLogin()

    /**
     * Sincronizza il valore di `is_biometric_enabled` sulla tabella `users` di Supabase.
     *
     * @param enabled il valore da aggiornare nel backend.
     */
    suspend fun syncBiometricPreferenceToBackend(enabled: Boolean)
}
