package com.wodtimer.app.data.repository

import com.wodtimer.app.data.local.dao.WorkoutHistoryDao
import com.wodtimer.app.data.local.entity.WorkoutHistoryEntity
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.WorkoutHistory
import com.wodtimer.app.domain.repository.WorkoutHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutHistoryRepositoryImpl @Inject constructor(
    private val dao: WorkoutHistoryDao
) : WorkoutHistoryRepository {

    override fun getAllHistory(): Flow<List<WorkoutHistory>> {
        return dao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHistoryByMode(mode: String): Flow<List<WorkoutHistory>> {
        return dao.getHistoryByMode(mode).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertHistory(history: WorkoutHistory): Long {
        return dao.insertHistory(history.toEntity())
    }

    override suspend fun deleteHistory(history: WorkoutHistory) {
        dao.deleteHistory(history.toEntity())
    }

    override suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }

    private fun WorkoutHistoryEntity.toDomain() = WorkoutHistory(
        id = id,
        workoutTitle = workoutTitle,
        mode = try { TimerMode.valueOf(mode) } catch (e: Exception) { TimerMode.FOR_TIME },
        wodText = wodText,
        elapsedMillis = elapsedMillis,
        roundsCompleted = roundsCompleted,
        notes = notes,
        completedAt = completedAt,
        isFavorite = isFavorite
    )

    private fun WorkoutHistory.toEntity() = WorkoutHistoryEntity(
        id = id,
        workoutTitle = workoutTitle,
        mode = mode.name,
        wodText = wodText,
        elapsedMillis = elapsedMillis,
        roundsCompleted = roundsCompleted,
        notes = notes,
        completedAt = completedAt,
        isFavorite = isFavorite
    )
}
