package com.wodtimer.app.domain.repository

import com.wodtimer.app.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<Workout>>
    fun getFavoriteWorkouts(): Flow<List<Workout>>
    suspend fun getWorkoutById(id: Long): Workout?
    suspend fun saveWorkout(workout: Workout): Long
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workout: Workout)
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun deleteById(id: Long)
}
