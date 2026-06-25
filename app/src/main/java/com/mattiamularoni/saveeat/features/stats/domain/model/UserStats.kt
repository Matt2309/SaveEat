package com.mattiamularoni.saveeat.features.stats.domain.model

/**
 * Modello di dominio per le statistiche di risparmio dell'utente.
 *
 * Totali accumulati cucinando ricette generate dall'app: ogni ricetta porta
 * con sé una stima di peso (kg) e costo (euro) "salvati" rispetto allo spreco,
 * che vengono somministrati a questi totali quando la ricetta viene marcata come cucinata.
 */
data class UserStats(
    val totalKgSaved: Double = 0.0,
    val totalEurosSaved: Double = 0.0,
    val totalEcoPoints: Int = 0,
)
