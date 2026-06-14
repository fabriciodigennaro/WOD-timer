package com.wodtimer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wodtimer.app.domain.model.TimerMode

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val mode: String = TimerMode.FOR_TIME.name,
    val wodText: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val rounds: Int = 0,
    val blocksJson: String = ""
)
