package com.wodtimer.app.domain.model

data class WorkoutHistory(
    val id: Long = 0,
    val workoutTitle: String = "",
    val mode: TimerMode = TimerMode.FOR_TIME,
    val wodText: String = "",
    val elapsedMillis: Long = 0L,
    val roundsCompleted: Int = 0,
    val notes: String = "",
    val completedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
