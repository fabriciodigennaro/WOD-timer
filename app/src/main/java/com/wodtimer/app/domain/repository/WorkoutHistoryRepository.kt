package com.wodtimer.app.domain.repository

import com.wodtimer.app.domain.model.WorkoutHistory
import kotlinx.coroutines.flow.Flow

interface WorkoutHistoryRepository {
    fun getAllHistory(): Flow<List<WorkoutHistory>>
    fun getHistoryByMode(mode: String): Flow<List<WorkoutHistory>>
    suspend fun insertHistory(history: WorkoutHistory): Long
    suspend fun deleteHistory(history: WorkoutHistory)
    suspend fun clearAllHistory()
}
