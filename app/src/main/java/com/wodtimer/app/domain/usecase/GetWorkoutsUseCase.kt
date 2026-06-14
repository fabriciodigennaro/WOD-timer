package com.wodtimer.app.domain.usecase

import com.wodtimer.app.domain.model.Workout
import com.wodtimer.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWorkoutsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    fun getAllWorkouts(): Flow<List<Workout>> = repository.getAllWorkouts()
    fun getFavoriteWorkouts(): Flow<List<Workout>> = repository.getFavoriteWorkouts()
}
