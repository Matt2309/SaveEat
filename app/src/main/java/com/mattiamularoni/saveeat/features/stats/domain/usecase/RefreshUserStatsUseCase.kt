package com.mattiamularoni.saveeat.features.stats.domain.usecase

import com.mattiamularoni.saveeat.features.stats.domain.repository.StatsRepository

/**
 * Use case per sincronizzare i totali di risparmio (kg, euro, eco-punti) dell'utente
 * corrente da Supabase verso la cache Room.
 *
 * Va invocato all'apertura di schermate che mostrano questi totali (Home, Profilo),
 * cosicché [GetUserStatsUseCase] (che osserva solo Room) rifletta la riga remota anche
 * per utenti che non hanno mai cucinato una ricetta su questo device.
 */
class RefreshUserStatsUseCase(
    private val statsRepository: StatsRepository,
) {
    suspend operator fun invoke(): Result<Unit> = statsRepository.refreshUserStats()
}
