package com.wodtimer.app.domain.model

data class TimerState(
    val mode: TimerMode = TimerMode.FOR_TIME,
    val phase: TimerPhase = TimerPhase.PREPARE,
    val elapsedMillis: Long = 0L,
    val totalDurationMillis: Long = 0L,
    val currentRound: Int = 0,
    val totalRounds: Int = 0,
    val currentSet: Int = 0,
    val totalSets: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val workSeconds: Int = 20,
    val restSeconds: Int = 10,
    val prepareSeconds: Int = 5,
    val intervalMinutes: Int = 1,
    val timeCapMinutes: Int = 0,
    val manualRounds: Int = 0,
    val wodText: String = "",
    val wodTitle: String = "",
    val blocks: List<TimerBlock> = emptyList(),
    val currentBlockIndex: Int = 0
)

data class TimerBlock(
    val type: BlockType,
    val durationSeconds: Int,
    val label: String = ""
)

enum class BlockType {
    WARMUP, WORK, REST, COOLDOWN
}
