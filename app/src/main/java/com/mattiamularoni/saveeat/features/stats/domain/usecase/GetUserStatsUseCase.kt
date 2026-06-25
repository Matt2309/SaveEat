package com.mattiamularoni.saveeat.features.stats.domain.usecase

import com.mattiamularoni.saveeat.features.stats.domain.model.UserStats
import com.mattiamularoni.saveeat.features.stats.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case per osservare i totali di risparmio (kg, euro) dell'utente corrente.
 *
 * Usato sia dal Dashboard (kg salvati) che dal Profilo (euro risparmiati).
 */
class GetUserStatsUseCase(
    private val statsRepository: StatsRepository,
) {
    operator fun invoke(): Flow<UserStats> = statsRepository.getUserStats()
}
