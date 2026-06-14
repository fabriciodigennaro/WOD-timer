package com.wodtimer.app.data.local.dao

import androidx.room.*
import com.wodtimer.app.data.local.entity.WorkoutHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutHistoryDao {
    @Query("SELECT * FROM workout_history ORDER BY completedAt DESC")
    fun getAllHistory(): Flow<List<WorkoutHistoryEntity>>

    @Query("SELECT * FROM workout_history WHERE mode = :mode ORDER BY completedAt DESC")
    fun getHistoryByMode(mode: String): Flow<List<WorkoutHistoryEntity>>

    @Query("SELECT * FROM workout_history WHERE isFavorite = 1 ORDER BY completedAt DESC")
    fun getFavoriteHistory(): Flow<List<WorkoutHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WorkoutHistoryEntity): Long

    @Delete
    suspend fun deleteHistory(history: WorkoutHistoryEntity)

    @Query("DELETE FROM workout_history")
    suspend fun clearAllHistory()
}
