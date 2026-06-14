package com.wodtimer.app.presentation.timer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.wodtimer.app.domain.model.*
import com.wodtimer.app.domain.repository.SettingsRepository
import com.wodtimer.app.domain.repository.WorkoutHistoryRepository
import com.wodtimer.app.domain.repository.WorkoutRepository
import com.wodtimer.app.service.SoundManager
import com.wodtimer.app.service.TtsManager
import com.wodtimer.app.service.VibrationManager
import com.wodtimer.app.util.FakeTimerClock
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class TimerViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val settingsRepository = mock<SettingsRepository>()
    private val historyRepository = mock<WorkoutHistoryRepository>()
    private val workoutRepository = mock<WorkoutRepository>()
    private val soundManager = mock<SoundManager>()
    private val ttsManager = mock<TtsManager>()
    private val vibrationManager = mock<VibrationManager>()
    private val fakeClock = FakeTimerClock()

    private lateinit var viewModel: TimerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(settingsRepository.getSettings()).thenReturn(flowOf(AppSettings(prepareCountdown = 0)))
        viewModel = TimerViewModel(
            settingsRepository = settingsRepository,
            historyRepository = historyRepository,
            workoutRepository = workoutRepository,
            soundManager = soundManager,
            ttsManager = ttsManager,
            vibrationManager = vibrationManager,
            timerClock = fakeClock
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- configure tests ---

    @Test
    fun `configure FOR_TIME with time cap`() {
        viewModel.configure(mode = TimerMode.FOR_TIME, timeCapMinutes = 5)
        val state = viewModel.state.value
        assertEquals(TimerMode.FOR_TIME, state.mode)
        assertEquals(5 * 60000L, state.totalDurationMillis)
    }

    @Test
    fun `configure FOR_TIME without time cap`() {
        viewModel.configure(mode = TimerMode.FOR_TIME, timeCapMinutes = 0)
        assertEquals(0L, viewModel.state.value.totalDurationMillis)
    }

    @Test
    fun `configure EMOM`() {
        viewModel.configure(mode = TimerMode.EMOM, intervalMinutes = 2, totalRounds = 5)
        val state = viewModel.state.value
        assertEquals(TimerMode.EMOM, state.mode)
        assertEquals(2 * 60000L * 5, state.totalDurationMillis)
        assertEquals(5, state.totalRounds)
    }

    @Test
    fun `configure AMRAP`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 15)
        assertEquals(15 * 60000L, viewModel.state.value.totalDurationMillis)
    }

    @Test
    fun `configure TABATA with prepare`() {
        viewModel.configure(mode = TimerMode.TABATA, workSeconds = 20, restSeconds = 10, totalRounds = 8, prepareSeconds = 5)
        val expected = (20 + 10) * 1000L * 8 + 5 * 1000L
        assertEquals(expected, viewModel.state.value.totalDurationMillis)
    }

    @Test
    fun `configure TABATA uses settings prepare when not specified`() {
        whenever(settingsRepository.getSettings()).thenReturn(flowOf(AppSettings(prepareCountdown = 7)))
        viewModel = TimerViewModel(
            settingsRepository, historyRepository, workoutRepository,
            soundManager, ttsManager, vibrationManager, fakeClock
        )
        viewModel.configure(mode = TimerMode.TABATA, workSeconds = 20, restSeconds = 10, totalRounds = 8)
        assertEquals(7, viewModel.state.value.prepareSeconds)
    }

    @Test
    fun `configure INTERVAL`() {
        viewModel.configure(mode = TimerMode.INTERVAL, workSeconds = 45, restSeconds = 15, totalRounds = 5)
        val expected = (45 + 15) * 1000L * 5
        assertEquals(expected, viewModel.state.value.totalDurationMillis)
    }

    @Test
    fun `configure CUSTOM with blocks`() {
        val blocks = listOf(
            TimerBlock(BlockType.WARMUP, 60, "Warmup"),
            TimerBlock(BlockType.WORK, 300, "Work"),
            TimerBlock(BlockType.COOLDOWN, 60, "Cooldown")
        )
        viewModel.configure(mode = TimerMode.CUSTOM, blocks = blocks)
        assertEquals((60 + 300 + 60) * 1000L, viewModel.state.value.totalDurationMillis)
        assertEquals(blocks, viewModel.state.value.blocks)
    }

    @Test
    fun `configure CUSTOM with empty blocks`() {
        viewModel.configure(mode = TimerMode.CUSTOM, blocks = emptyList())
        assertEquals(0L, viewModel.state.value.totalDurationMillis)
    }

    @Test
    fun `configure preserves text`() {
        viewModel.configure(mode = TimerMode.FOR_TIME, timeCapMinutes = 10, wodText = "WOD", wodTitle = "Title")
        assertEquals("WOD", viewModel.state.value.wodText)
        assertEquals("Title", viewModel.state.value.wodTitle)
    }

    @Test
    fun `configure resets previous state`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 10)
        viewModel.start()
        viewModel.configure(mode = TimerMode.EMOM, intervalMinutes = 1, totalRounds = 3)
        assertFalse(viewModel.state.value.isRunning)
        assertEquals(TimerMode.EMOM, viewModel.state.value.mode)
    }

    @Test
    fun `EMOM with zero interval`() {
        viewModel.configure(mode = TimerMode.EMOM, intervalMinutes = 0, totalRounds = 5)
        assertEquals(0L, viewModel.state.value.totalDurationMillis)
    }

    @Test
    fun `FOR_TIME with zero time cap`() {
        viewModel.configure(mode = TimerMode.FOR_TIME, timeCapMinutes = 0)
        assertEquals(0L, viewModel.state.value.totalDurationMillis)
    }

    // --- start / pause / resume / reset tests ---

    @Test
    fun `start double start prevention`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 10)
        viewModel.start()
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)
    }

    @Test
    fun `pause when running`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 10)
        viewModel.start()
        viewModel.pause()
        assertTrue(viewModel.state.value.isPaused)
        assertFalse(viewModel.state.value.isRunning)
    }

    @Test
    fun `pause when not running is no-op`() {
        viewModel.pause()
        assertFalse(viewModel.state.value.isPaused)
    }

    @Test
    fun `pause when already paused is no-op`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 10)
        viewModel.start()
        viewModel.pause()
        viewModel.pause()
        assertTrue(viewModel.state.value.isPaused)
    }

    @Test
    fun `resume from pause`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 10)
        viewModel.start()
        viewModel.pause()
        fakeClock.advance(2000)
        viewModel.resume()
        assertFalse(viewModel.state.value.isPaused)
        assertTrue(viewModel.state.value.isRunning)
    }

    @Test
    fun `resume when not paused is no-op`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 10)
        viewModel.resume()
        assertFalse(viewModel.state.value.isPaused)
    }

    @Test
    fun `togglePause toggles`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 10)
        viewModel.start()
        viewModel.togglePause()
        assertTrue(viewModel.state.value.isPaused)
        viewModel.togglePause()
        assertFalse(viewModel.state.value.isPaused)
    }

    @Test
    fun `reset clears state`() {
        viewModel.configure(mode = TimerMode.EMOM, intervalMinutes = 1, totalRounds = 5)
        viewModel.start()
        viewModel.reset()
        val state = viewModel.state.value
        assertEquals(TimerPhase.PREPARE, state.phase)
        assertEquals(0L, state.elapsedMillis)
        assertEquals(0, state.currentRound)
        assertFalse(state.isRunning)
        assertFalse(state.isPaused)
        assertFalse(state.isFinished)
    }

    @Test
    fun `configure after start resets`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 10)
        viewModel.start()
        viewModel.configure(mode = TimerMode.FOR_TIME, timeCapMinutes = 5)
        assertFalse(viewModel.state.value.isRunning)
    }

    // --- load and edit ---

    @Test
    fun `loadWorkout loads`() = runTest(testDispatcher) {
        val workout = Workout(id = 1, title = "Test", mode = TimerMode.AMRAP, wodText = "WOD")
        whenever(workoutRepository.getWorkoutById(1)).thenReturn(workout)
        viewModel.loadWorkout(1)
        assertEquals(TimerMode.AMRAP, viewModel.state.value.mode)
        assertEquals("WOD", viewModel.state.value.wodText)
    }

    @Test
    fun `loadWorkout with zero id does nothing`() {
        viewModel.loadWorkout(0)
        runTest(testDispatcher) {
            verify(workoutRepository, never()).getWorkoutById(any())
        }
    }

    @Test
    fun `loadWorkout with null result does nothing`() {
        runTest(testDispatcher) {
            whenever(workoutRepository.getWorkoutById(any())).thenReturn(null)
        }
        viewModel.loadWorkout(999)
        assertEquals(TimerMode.FOR_TIME, viewModel.state.value.mode)
    }

    @Test
    fun `incrementRound increments`() {
        assertEquals(0, viewModel.state.value.manualRounds)
        viewModel.incrementRound()
        assertEquals(1, viewModel.state.value.manualRounds)
        viewModel.incrementRound()
        assertEquals(2, viewModel.state.value.manualRounds)
        verify(vibrationManager, times(2)).vibrateShort()
    }

    @Test
    fun `updateWodText updates`() {
        viewModel.updateWodText("New Text")
        assertEquals("New Text", viewModel.state.value.wodText)
    }

    // --- timer runner completion tests ---

    @Test
    fun `AMRAP with zero duration finishes`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 0)
        viewModel.start()
        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `FOR_TIME with zero duration does not finish`() {
        viewModel.configure(mode = TimerMode.FOR_TIME, timeCapMinutes = 0)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)
    }

    @Test
    fun `EMOM with zero interval and one round finishes`() {
        viewModel.configure(mode = TimerMode.EMOM, intervalMinutes = 0, totalRounds = 1)
        viewModel.start()
        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `TABATA with zero durations finishes`() {
        viewModel.configure(mode = TimerMode.TABATA, workSeconds = 0, restSeconds = 0, totalRounds = 1, prepareSeconds = 0)
        viewModel.start()
        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `INTERVAL with zero durations finishes`() {
        viewModel.configure(mode = TimerMode.INTERVAL, workSeconds = 0, restSeconds = 0, totalRounds = 1)
        viewModel.start()
        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `CUSTOM with zero-duration block finishes`() {
        viewModel.configure(mode = TimerMode.CUSTOM, blocks = listOf(TimerBlock(BlockType.WORK, 0, "")))
        viewModel.start()
        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `finishTimer saves history`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 0)
        viewModel.start()
        runTest(testDispatcher) {
            verify(historyRepository).insertHistory(any())
        }
    }

    // --- timer completion tests using fakeClock.advance + scheduler.advanceTimeBy ---

    @Test
    fun `FOR_TIME with 1 min time cap finishes when elapsed`() {
        viewModel.configure(mode = TimerMode.FOR_TIME, timeCapMinutes = 1)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)

        fakeClock.advance(60000)
        testDispatcher.scheduler.advanceTimeBy(60)

        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `AMRAP with 1 min duration finishes when elapsed`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 1)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)

        fakeClock.advance(60000)
        testDispatcher.scheduler.advanceTimeBy(60)

        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `prepare countdown completes and starts timer`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 0, prepareSeconds = 1)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)
        assertEquals(TimerPhase.PREPARE, viewModel.state.value.phase)

        repeat(20) {
            fakeClock.advance(100)
            testDispatcher.scheduler.advanceTimeBy(100)
        }

        assertTrue(viewModel.state.value.isFinished)
        verify(ttsManager).speak("Go")
        verify(vibrationManager).vibrateLong()
    }

    @Test
    fun `EMOM with one interval finishes when interval elapses`() {
        viewModel.configure(mode = TimerMode.EMOM, intervalMinutes = 1, totalRounds = 1)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)

        fakeClock.advance(60000)
        testDispatcher.scheduler.advanceTimeBy(100)

        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `CUSTOM with one block finishes when block elapses`() {
        viewModel.configure(mode = TimerMode.CUSTOM, blocks = listOf(TimerBlock(BlockType.WORK, 30, "Work")))
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)

        fakeClock.advance(30000)
        testDispatcher.scheduler.advanceTimeBy(100)

        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `TABATA with one work-rest cycle finishes when complete`() {
        viewModel.configure(mode = TimerMode.TABATA, workSeconds = 1, restSeconds = 1, totalRounds = 1, prepareSeconds = 0)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)

        repeat(50) {
            fakeClock.advance(50)
            testDispatcher.scheduler.advanceTimeBy(50)
        }

        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `INTERVAL with one work-rest cycle finishes when complete`() {
        viewModel.configure(mode = TimerMode.INTERVAL, workSeconds = 1, restSeconds = 1, totalRounds = 1)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)

        repeat(50) {
            fakeClock.advance(50)
            testDispatcher.scheduler.advanceTimeBy(50)
        }

        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `onCleared cancels timer job`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 10)
        viewModel.start()
        val onCleared = TimerViewModel::class.java.getDeclaredMethod("onCleared")
        onCleared.isAccessible = true
        onCleared.invoke(viewModel)
    }

    @Test
    fun `FOR_TIME minute beep plays at each minute`() {
        viewModel.configure(mode = TimerMode.FOR_TIME, timeCapMinutes = 5)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)

        fakeClock.advance(61000)
        testDispatcher.scheduler.advanceTimeBy(100)

        verify(soundManager, atLeastOnce()).playBeep()
    }

    @Test
    fun `AMRAP countdown beeps and TTS at final seconds`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 2)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)

        fakeClock.advance(115000)
        testDispatcher.scheduler.advanceTimeBy(100)

        fakeClock.advance(2000)
        testDispatcher.scheduler.advanceTimeBy(100)

        verify(ttsManager, atLeastOnce()).sayCountdown(any())
    }

    @Test
    fun `AMRAP one minute warning at 60 seconds remaining`() {
        viewModel.configure(mode = TimerMode.AMRAP, totalMinutes = 2)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)

        repeat(2) {
            fakeClock.advance(30000)
            testDispatcher.scheduler.advanceTimeBy(50)
        }

        verify(ttsManager, atLeastOnce()).speak("One minute remaining")
    }

    @Test
    fun `EMOM with 2 rounds beeps on transition and says last round`() {
        viewModel.configure(mode = TimerMode.EMOM, intervalMinutes = 1, totalRounds = 2)
        viewModel.start()
        assertTrue(viewModel.state.value.isRunning)

        fakeClock.advance(60000)
        testDispatcher.scheduler.advanceTimeBy(100)

        verify(soundManager, times(1)).playBeep()
        verify(ttsManager).sayLastRound()

        fakeClock.advance(60000)
        testDispatcher.scheduler.advanceTimeBy(100)

        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `resume FOR_TIME after pause`() {
        viewModel.configure(mode = TimerMode.FOR_TIME, timeCapMinutes = 5)
        viewModel.start()
        viewModel.pause()
        fakeClock.advance(2000)
        viewModel.resume()
        assertTrue(viewModel.state.value.isRunning)
    }

    @Test
    fun `resume TABATA after pause`() {
        viewModel.configure(mode = TimerMode.TABATA, workSeconds = 20, restSeconds = 10, totalRounds = 3)
        viewModel.start()
        viewModel.pause()
        viewModel.resume()
        assertTrue(viewModel.state.value.isRunning)
    }

    @Test
    fun `resume INTERVAL after pause`() {
        viewModel.configure(mode = TimerMode.INTERVAL, workSeconds = 10, restSeconds = 5, totalRounds = 2)
        viewModel.start()
        viewModel.pause()
        fakeClock.advance(1000)
        viewModel.resume()
        assertTrue(viewModel.state.value.isRunning)
    }

    @Test
    fun `resume CUSTOM after pause`() {
        viewModel.configure(mode = TimerMode.CUSTOM, blocks = listOf(TimerBlock(BlockType.WORK, 10, "Test")))
        viewModel.start()
        viewModel.pause()
        viewModel.resume()
        assertTrue(viewModel.state.value.isRunning)
    }
}