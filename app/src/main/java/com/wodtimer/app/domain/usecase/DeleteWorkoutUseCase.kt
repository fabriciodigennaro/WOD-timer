package com.wodtimer.app.domain.usecase

import com.wodtimer.app.domain.repository.WorkoutRepository
import javax.inject.Inject

class DeleteWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteById(id)
    }
}
