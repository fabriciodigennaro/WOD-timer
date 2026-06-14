package com.wodtimer.app.domain.model

data class Workout(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val mode: TimerMode = TimerMode.FOR_TIME,
    val wodText: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val rounds: Int = 0,
    val blocks: List<TimerBlock> = emptyList()
)
