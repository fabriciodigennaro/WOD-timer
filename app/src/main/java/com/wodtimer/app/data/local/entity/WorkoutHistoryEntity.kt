package com.wodtimer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wodtimer.app.domain.model.TimerMode

@Entity(tableName = "workout_history")
data class WorkoutHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutTitle: String = "",
    val mode: String = TimerMode.FOR_TIME.name,
    val wodText: String = "",
    val elapsedMillis: Long = 0L,
    val roundsCompleted: Int = 0,
    val notes: String = "",
    val completedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
