package com.wodtimer.app.presentation.workout

import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.Workout
import com.wodtimer.app.domain.repository.WorkoutRepository
import com.wodtimer.app.domain.usecase.GetWorkoutsUseCase
import com.wodtimer.app.domain.usecase.SaveWorkoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class WorkoutEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val saveWorkoutUseCase: SaveWorkoutUseCase = mock()
    private val getWorkoutsUseCase: GetWorkoutsUseCase = mock()
    private val workoutRepository: WorkoutRepository = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is blank`() = runTest(testDispatcher) {
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertEquals(TimerMode.FOR_TIME, state.mode)
        assertEquals("", state.wodText)
        assertEquals(0, state.durationSeconds)
        assertEquals(0, state.rounds)
        assertTrue(state.blocks.isEmpty())
        assertFalse(state.isSaved)
        assertFalse(state.isExisting)
    }

    @Test
    fun `loadWorkout with positive id populates state`() = runTest(testDispatcher) {
        val workout = Workout(
            id = 5,
            title = "Test WOD",
            description = "A test",
            mode = TimerMode.AMRAP,
            wodText = "Do work",
            durationSeconds = 600,
            rounds = 3
        )
        whenever(workoutRepository.getWorkoutById(5L)).thenReturn(workout)

        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.loadWorkout(5L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Test WOD", state.title)
        assertEquals("A test", state.description)
        assertEquals(TimerMode.AMRAP, state.mode)
        assertEquals("Do work", state.wodText)
        assertEquals(600, state.durationSeconds)
        assertEquals(3, state.rounds)
        assertTrue(state.isExisting)
    }

    @Test
    fun `loadWorkout with id zero does nothing`() = runTest(testDispatcher) {
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.loadWorkout(0L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertFalse(state.isExisting)
    }

    @Test
    fun `loadWorkout with negative id does nothing`() = runTest(testDispatcher) {
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.loadWorkout(-1L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertFalse(state.isExisting)
    }

    @Test
    fun `loadWorkout with null repository result does nothing`() = runTest(testDispatcher) {
        whenever(workoutRepository.getWorkoutById(any())).thenReturn(null)

        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.loadWorkout(10L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertFalse(state.isExisting)
    }

    @Test
    fun `updateTitle updates title`() = runTest(testDispatcher) {
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.updateTitle("New Title")
        assertEquals("New Title", viewModel.uiState.value.title)
    }

    @Test
    fun `updateDescription updates description`() = runTest(testDispatcher) {
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.updateDescription("New Description")
        assertEquals("New Description", viewModel.uiState.value.description)
    }

    @Test
    fun `updateMode updates mode`() = runTest(testDispatcher) {
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.updateMode(TimerMode.TABATA)
        assertEquals(TimerMode.TABATA, viewModel.uiState.value.mode)
    }

    @Test
    fun `updateWodText updates wodText`() = runTest(testDispatcher) {
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.updateWodText("New WOD text")
        assertEquals("New WOD text", viewModel.uiState.value.wodText)
    }

    @Test
    fun `updateDuration updates durationSeconds`() = runTest(testDispatcher) {
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.updateDuration(300)
        assertEquals(300, viewModel.uiState.value.durationSeconds)
    }

    @Test
    fun `updateRounds updates rounds`() = runTest(testDispatcher) {
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.updateRounds(5)
        assertEquals(5, viewModel.uiState.value.rounds)
    }

    @Test
    fun `saveWorkout calls usecase and invokes callback`() = runTest(testDispatcher) {
        val onSaved: () -> Unit = mock()
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        viewModel.updateTitle("My WOD")
        viewModel.updateMode(TimerMode.EMOM)

        viewModel.saveWorkout(onSaved)
        advanceUntilIdle()

        verify(saveWorkoutUseCase).invoke(
            argThat { workout ->
                workout.title == "My WOD" && workout.mode == TimerMode.EMOM
            }
        )
        verify(onSaved).invoke()
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `saveWorkout with blank title defaults to Untitled WOD`() = runTest(testDispatcher) {
        val onSaved: () -> Unit = mock()
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)

        viewModel.saveWorkout(onSaved)
        advanceUntilIdle()

        verify(saveWorkoutUseCase).invoke(
            argThat { workout ->
                workout.title == "Untitled WOD"
            }
        )
    }

    @Test
    fun `modes list is complete`() = runTest(testDispatcher) {
        val viewModel = WorkoutEditorViewModel(saveWorkoutUseCase, getWorkoutsUseCase, workoutRepository)
        assertEquals(TimerMode.entries.toList(), viewModel.modes)
    }
}