package com.wodtimer.app.domain.usecase

import com.wodtimer.app.domain.repository.WorkoutRepository
import com.wodtimer.app.domain.repository.WorkoutHistoryRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(id: Long, isFavorite: Boolean) {
        workoutRepository.toggleFavorite(id, isFavorite)
    }
}
