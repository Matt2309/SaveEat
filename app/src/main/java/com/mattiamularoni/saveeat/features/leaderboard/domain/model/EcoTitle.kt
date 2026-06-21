package com.mattiamularoni.saveeat.features.leaderboard.domain.model

/**
 * Titolo testuale assegnato in base agli eco-punti accumulati dall'utente.
 *
 * Le soglie (minPoints) sono crescenti: fromPoints() restituisce il titolo
 * più alto il cui minPoints è <= ai punti dell'utente.
 */
enum class EcoTitle(val label: String, val minPoints: Int) {
    NOVELLINO("Novellino", 0),
    APPRENDISTA_GREEN("Apprendista Green", 50),
    RISPARMIATORE("Risparmiatore", 150),
    GUARDIANO_DISPENSA("Guardiano della Dispensa", 350),
    ESPERTO_ANTI_SPRECO("Esperto Anti-Spreco", 700),
    MAESTRO_ECO("Maestro Eco", 1250),
    EROE_DEL_PIANETA("Eroe del Pianeta", 2500),
    LEGGENDA_SAVEEAT("Leggenda SaveEat", 5000);

    companion object {
        fun fromPoints(points: Int): EcoTitle = entries.last { points >= it.minPoints }
    }
}
