package com.wodtimer.app.presentation.timer

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wodtimer.app.domain.model.*
import com.wodtimer.app.domain.repository.SettingsRepository
import com.wodtimer.app.domain.repository.WorkoutHistoryRepository
import com.wodtimer.app.domain.repository.WorkoutRepository
import com.wodtimer.app.service.SoundManager
import com.wodtimer.app.service.TtsManager
import com.wodtimer.app.service.VibrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: WorkoutHistoryRepository,
    private val workoutRepository: WorkoutRepository,
    private val soundManager: SoundManager,
    private val ttsManager: TtsManager,
    private val vibrationManager: VibrationManager
) : ViewModel() {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var startTimestamp: Long = 0L
    private var pausedElapsed: Long = 0L
    private var lastBeepMinute: Int = -1
    private var lastTabataPhase: TimerPhase? = null
    private var settings: AppSettings = AppSettings()

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { s ->
                settings = s
                soundManager.updateSettings(s)
                ttsManager.updateSettings(s)
                vibrationManager.updateSettings(s)
            }
        }
    }

    fun loadWorkout(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutById(id) ?: return@launch
            configure(
                mode = workout.mode,
                wodText = workout.wodText,
                wodTitle = workout.title,
                totalMinutes = workout.durationSeconds / 60,
                totalRounds = workout.rounds,
                blocks = workout.blocks
            )
        }
    }

    fun configure(
        mode: TimerMode,
        wodText: String = "",
        wodTitle: String = "",
        workSeconds: Int = 20,
        restSeconds: Int = 10,
        prepareSeconds: Int = 0,
        totalMinutes: Int = 10,
        totalRounds: Int = 8,
        intervalMinutes: Int = 1,
        timeCapMinutes: Int = 0,
        blocks: List<TimerBlock> = emptyList()
    ) {
        reset()
        val actualPrepareSeconds = if (prepareSeconds > 0) prepareSeconds else settings.prepareCountdown
        val totalMillis = when (mode) {
            TimerMode.AMRAP -> minutesToMillis(totalMinutes)
            TimerMode.EMOM -> intervalMinutes * 60000L * totalRounds
            TimerMode.FOR_TIME -> if (timeCapMinutes > 0) minutesToMillis(timeCapMinutes) else 0L
            TimerMode.TABATA -> (workSeconds + restSeconds) * 1000L * totalRounds + actualPrepareSeconds * 1000L
            TimerMode.INTERVAL -> (workSeconds + restSeconds) * 1000L * totalRounds
            TimerMode.CUSTOM -> blocks.sumOf { it.durationSeconds * 1000L }
        }

        _state.value = _state.value.copy(
            mode = mode,
            wodText = wodText,
            wodTitle = wodTitle,
            workSeconds = workSeconds,
            restSeconds = restSeconds,
            prepareSeconds = actualPrepareSeconds,
            totalDurationMillis = totalMillis,
            totalRounds = totalRounds,
            intervalMinutes = intervalMinutes,
            timeCapMinutes = timeCapMinutes,
            blocks = blocks
        )
    }

    fun start() {
        if (_state.value.isRunning) return

        if (_state.value.prepareSeconds > 0 && _state.value.phase == TimerPhase.PREPARE) {
            startPrepareCountdown()
        } else {
            startTimer()
        }
    }

    private fun startPrepareCountdown() {
        _state.value = _state.value.copy(isRunning = true, phase = TimerPhase.PREPARE)
        timerJob = viewModelScope.launch {
            val prepareMs = _state.value.prepareSeconds * 1000L
            val phaseStart = SystemClock.elapsedRealtime()
            var lastAnnouncedSecond = -1

            while (currentCoroutineContext().isActive) {
                val elapsed = SystemClock.elapsedRealtime() - phaseStart
                val remaining = prepareMs - elapsed
                val secondsRemaining = ((remaining + 999) / 1000).toInt()

                _state.value = _state.value.copy(elapsedMillis = remaining)

                if (remaining <= 0) break

                if (secondsRemaining in 1..3 && secondsRemaining != lastAnnouncedSecond) {
                    lastAnnouncedSecond = secondsRemaining
                    ttsManager.sayCountdown(secondsRemaining)
                    vibrationManager.vibrateShort()
                }

                delay(100)
            }

            ttsManager.speak("Go")
            vibrationManager.vibrateLong()
            startTimer()
        }
    }

    private fun startTimer() {
        _state.value = _state.value.copy(
            isRunning = true,
            isPaused = false,
            isFinished = false,
            phase = getWorkPhase()
        )
        lastBeepMinute = -1
        lastTabataPhase = null
        startTimestamp = SystemClock.elapsedRealtime()
        pausedElapsed = 0L

        timerJob = viewModelScope.launch {
            when (_state.value.mode) {
                TimerMode.FOR_TIME -> runForTime()
                TimerMode.EMOM -> runEmom()
                TimerMode.TABATA -> runTabata()
                TimerMode.AMRAP -> runAmrap()
                TimerMode.INTERVAL -> runInterval()
                TimerMode.CUSTOM -> runCustom()
            }
        }
    }

    private suspend fun runForTime() {
        val totalDurationMs = _state.value.totalDurationMillis

        while (currentCoroutineContext().isActive) {
            val elapsed = SystemClock.elapsedRealtime() - startTimestamp + pausedElapsed
            _state.value = _state.value.copy(
                elapsedMillis = elapsed,
                phase = TimerPhase.WORK
            )

            if (totalDurationMs > 0 && elapsed >= totalDurationMs) {
                finishTimer()
                return
            }

            val currentMinute = (elapsed / 60000).toInt()
            if (currentMinute > lastBeepMinute && settings.beepAtEachMinute) {
                lastBeepMinute = currentMinute
                soundManager.playBeep()
            }

            delay(30)
        }
    }

    private suspend fun runEmom() {
        val intervalMs = _state.value.intervalMinutes * 60000L
        val totalRounds = _state.value.totalRounds
        var currentRound = 0
        var lastBeepSecond = -1

        while (currentRound < totalRounds && currentCoroutineContext().isActive) {
            val roundStart = SystemClock.elapsedRealtime()
            lastBeepSecond = -1

            _state.value = _state.value.copy(
                currentRound = currentRound + 1,
                totalRounds = totalRounds,
                elapsedMillis = SystemClock.elapsedRealtime() - startTimestamp + pausedElapsed,
                phase = TimerPhase.WORK
            )

            if (currentRound > 0) {
                soundManager.playBeep()
            }
            if (currentRound == totalRounds - 1) {
                ttsManager.sayLastRound()
            }

            while (currentCoroutineContext().isActive) {
                val roundElapsed = SystemClock.elapsedRealtime() - roundStart

                _state.value = _state.value.copy(
                    elapsedMillis = SystemClock.elapsedRealtime() - startTimestamp + pausedElapsed
                )

                val remainingInRound = intervalMs - roundElapsed
                val secondsRemaining = (remainingInRound / 1000).toInt()

                if (secondsRemaining in 1..3 && secondsRemaining != lastBeepSecond) {
                    lastBeepSecond = secondsRemaining
                    soundManager.playBeep()
                    vibrationManager.vibrateShort()
                }

                if (roundElapsed >= intervalMs) {
                    currentRound++
                    if (currentRound >= totalRounds) {
                        finishTimer()
                        return
                    }
                    break
                }

                delay(50)
            }
        }
    }

    private suspend fun runTabata() {
        val workMs = _state.value.workSeconds * 1000L
        val restMs = _state.value.restSeconds * 1000L
        val totalRounds = _state.value.totalRounds

        var round = 0
        var isWork = true

        while (round < totalRounds && currentCoroutineContext().isActive) {
            val phaseStart = SystemClock.elapsedRealtime()
            val durationMs = if (isWork) workMs else restMs
            val phase = if (isWork) TimerPhase.WORK else TimerPhase.REST

            _state.value = _state.value.copy(
                currentRound = round + 1,
                totalRounds = totalRounds,
                phase = phase,
                elapsedMillis = SystemClock.elapsedRealtime() - startTimestamp + pausedElapsed
            )

            // Phase start sounds
            when (phase) {
                TimerPhase.WORK -> {
                    soundManager.playGoBeep()
                    if (round == totalRounds - 1) {
                        ttsManager.sayLastRound()
                    }
                }
                TimerPhase.REST -> {
                    soundManager.playRestBeep()
                    ttsManager.sayRest()
                    vibrationManager.vibrateShort()
                }
                else -> {}
            }

            while (SystemClock.elapsedRealtime() - phaseStart < durationMs && currentCoroutineContext().isActive) {
                _state.value = _state.value.copy(
                    elapsedMillis = SystemClock.elapsedRealtime() - startTimestamp + pausedElapsed
                )
                delay(50)
            }

            if (isWork) {
                isWork = false
            } else {
                isWork = true
                round++
            }
        }

        finishTimer()
    }

    private suspend fun runAmrap() {
        val durationMs = _state.value.totalDurationMillis

        while (currentCoroutineContext().isActive) {
            val elapsed = SystemClock.elapsedRealtime() - startTimestamp + pausedElapsed
            val remaining = durationMs - elapsed

            _state.value = _state.value.copy(
                elapsedMillis = elapsed,
                phase = if (remaining > 0) TimerPhase.WORK else TimerPhase.FINISHED
            )

            if (remaining <= 0) {
                finishTimer()
                return
            }

            // Warnings at 60s, 30s, 10s, 5,4,3,2,1
            val remainingSecs = (remaining / 1000).toInt()
            if (remainingSecs <= 5 && remainingSecs > 0 && remainingSecs != lastBeepMinute) {
                lastBeepMinute = remainingSecs
                soundManager.playBeep()
                if (remainingSecs <= 3) {
                    ttsManager.sayCountdown(remainingSecs)
                }
            }

            if (remainingSecs == 60 && settings.lastBeepWarning) {
                soundManager.playBeep()
                ttsManager.speak("One minute remaining")
            }

            delay(30)
        }
    }

    private suspend fun runInterval() {
        val workMs = _state.value.workSeconds * 1000L
        val restMs = _state.value.restSeconds * 1000L
        val totalSets = _state.value.totalRounds

        var set = 0
        var isWork = true

        while (set < totalSets && currentCoroutineContext().isActive) {
            val phaseStart = SystemClock.elapsedRealtime()
            val durationMs = if (isWork) workMs else restMs
            val phase = if (isWork) TimerPhase.WORK else TimerPhase.REST

            _state.value = _state.value.copy(
                currentSet = set + 1,
                totalSets = totalSets,
                phase = phase,
                elapsedMillis = SystemClock.elapsedRealtime() - startTimestamp + pausedElapsed
            )

            // Phase sounds
            when (phase) {
                TimerPhase.WORK -> {
                    soundManager.playGoBeep()
                }
                TimerPhase.REST -> {
                    soundManager.playRestBeep()
                    ttsManager.sayRest()
                    vibrationManager.vibrateShort()
                }
                else -> {}
            }

            while (SystemClock.elapsedRealtime() - phaseStart < durationMs && currentCoroutineContext().isActive) {
                _state.value = _state.value.copy(
                    elapsedMillis = SystemClock.elapsedRealtime() - startTimestamp + pausedElapsed
                )
                delay(50)
            }

            if (isWork) {
                isWork = false
            } else {
                isWork = true
                set++
            }
        }

        finishTimer()
    }

    private suspend fun runCustom() {
        val blocks = _state.value.blocks
        var blockIndex = 0

        while (blockIndex < blocks.size && currentCoroutineContext().isActive) {
            val block = blocks[blockIndex]
            val blockStart = SystemClock.elapsedRealtime()
            val durationMs = block.durationSeconds * 1000L

            _state.value = _state.value.copy(
                currentBlockIndex = blockIndex,
                phase = when (block.type) {
                    BlockType.WARMUP -> TimerPhase.PREPARE
                    BlockType.WORK -> TimerPhase.WORK
                    BlockType.REST -> TimerPhase.REST
                    BlockType.COOLDOWN -> TimerPhase.COOLDOWN
                },
                currentRound = blockIndex + 1,
                totalRounds = blocks.size
            )

            // Block start notification
            soundManager.playBeep()

            while (SystemClock.elapsedRealtime() - blockStart < durationMs && currentCoroutineContext().isActive) {
                _state.value = _state.value.copy(
                    elapsedMillis = SystemClock.elapsedRealtime() - startTimestamp + pausedElapsed
                )
                delay(50)
            }

            blockIndex++
        }

        finishTimer()
    }

    fun pause() {
        if (!_state.value.isRunning || _state.value.isPaused) return
        timerJob?.cancel()
        pausedElapsed += SystemClock.elapsedRealtime() - startTimestamp
        _state.value = _state.value.copy(isPaused = true, isRunning = false)
    }

    fun resume() {
        if (!_state.value.isPaused) return
        startTimestamp = SystemClock.elapsedRealtime()
        _state.value = _state.value.copy(isRunning = true, isPaused = false)

        timerJob = viewModelScope.launch {
            when (_state.value.mode) {
                TimerMode.FOR_TIME -> runForTime()
                TimerMode.EMOM -> runEmom()
                TimerMode.TABATA -> runTabata()
                TimerMode.AMRAP -> runAmrap()
                TimerMode.INTERVAL -> runInterval()
                TimerMode.CUSTOM -> runCustom()
            }
        }
    }

    fun togglePause() {
        if (_state.value.isPaused) resume() else pause()
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        pausedElapsed = 0L
        lastBeepMinute = -1
        _state.value = _state.value.copy(
            phase = TimerPhase.PREPARE,
            elapsedMillis = 0L,
            currentRound = 0,
            currentSet = 0,
            isRunning = false,
            isPaused = false,
            isFinished = false,
            manualRounds = 0
        )
    }

    fun incrementRound() {
        _state.value = _state.value.copy(manualRounds = _state.value.manualRounds + 1)
        vibrationManager.vibrateShort()
    }

    fun updateWodText(text: String) {
        _state.value = _state.value.copy(wodText = text)
    }

    private fun finishTimer() {
        timerJob?.cancel()
        _state.value = _state.value.copy(
            isRunning = false,
            isFinished = true,
            phase = TimerPhase.FINISHED
        )
        soundManager.playFinalBeep()
        vibrationManager.vibratePattern()
        ttsManager.sayFinished()

        // Save to history
        viewModelScope.launch {
            val history = WorkoutHistory(
                workoutTitle = _state.value.wodTitle.ifBlank { _state.value.mode.displayName },
                mode = _state.value.mode,
                wodText = _state.value.wodText,
                elapsedMillis = _state.value.elapsedMillis,
                roundsCompleted = if (_state.value.mode == TimerMode.AMRAP) _state.value.manualRounds else _state.value.currentRound
            )
            historyRepository.insertHistory(history)
        }
    }

    private fun getWorkPhase(): TimerPhase = when (_state.value.mode) {
        TimerMode.FOR_TIME -> TimerPhase.WORK
        TimerMode.EMOM -> TimerPhase.WORK
        TimerMode.TABATA -> TimerPhase.WORK
        TimerMode.AMRAP -> TimerPhase.WORK
        TimerMode.INTERVAL -> TimerPhase.WORK
        TimerMode.CUSTOM -> TimerPhase.WORK
    }

    private fun minutesToMillis(minutes: Int) = minutes * 60000L

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
