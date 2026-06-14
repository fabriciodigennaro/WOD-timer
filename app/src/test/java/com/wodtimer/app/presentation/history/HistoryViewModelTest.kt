package com.wodtimer.app.presentation.history

import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.WorkoutHistory
import com.wodtimer.app.domain.repository.WorkoutHistoryRepository
import com.wodtimer.app.domain.usecase.DeleteWorkoutUseCase
import com.wodtimer.app.domain.usecase.GetWorkoutsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val historyRepository: WorkoutHistoryRepository = mock()
    private val getWorkoutsUseCase: GetWorkoutsUseCase = mock()
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has All filter`() = runTest(testDispatcher) {
        whenever(historyRepository.getAllHistory()).thenReturn(flowOf(emptyList()))
        whenever(getWorkoutsUseCase.getAllWorkouts()).thenReturn(flowOf(emptyList()))

        val viewModel = HistoryViewModel(historyRepository, getWorkoutsUseCase, deleteWorkoutUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("All", state.selectedFilter)
        assertTrue(state.historyList.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `setFilter All gets all history`() = runTest(testDispatcher) {
        val allHistory = listOf(
            WorkoutHistory(id = 1, workoutTitle = "WOD 1"),
            WorkoutHistory(id = 2, workoutTitle = "WOD 2")
        )
        whenever(historyRepository.getAllHistory()).thenReturn(flowOf(allHistory))
        whenever(getWorkoutsUseCase.getAllWorkouts()).thenReturn(flowOf(emptyList()))

        val viewModel = HistoryViewModel(historyRepository, getWorkoutsUseCase, deleteWorkoutUseCase)
        advanceUntilIdle()

        viewModel.setFilter("All")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("All", state.selectedFilter)
        assertEquals(allHistory, state.historyList)
    }

    @Test
    fun `setFilter EMOM gets history by EMOM mode`() = runTest(testDispatcher) {
        val emomHistory = listOf(
            WorkoutHistory(id = 3, workoutTitle = "EMOM WOD", mode = TimerMode.EMOM)
        )
        whenever(historyRepository.getAllHistory()).thenReturn(flowOf(emptyList()))
        whenever(historyRepository.getHistoryByMode("EMOM")).thenReturn(flowOf(emomHistory))
        whenever(getWorkoutsUseCase.getAllWorkouts()).thenReturn(flowOf(emptyList()))

        val viewModel = HistoryViewModel(historyRepository, getWorkoutsUseCase, deleteWorkoutUseCase)
        advanceUntilIdle()

        viewModel.setFilter("EMOM")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("EMOM", state.selectedFilter)
        assertEquals(emomHistory, state.historyList)
        verify(historyRepository).getHistoryByMode("EMOM")
    }

    @Test
    fun `setFilter Unknown Mode edge case`() = runTest(testDispatcher) {
        whenever(historyRepository.getAllHistory()).thenReturn(flowOf(emptyList()))
        whenever(historyRepository.getHistoryByMode("")).thenReturn(flowOf(emptyList()))
        whenever(getWorkoutsUseCase.getAllWorkouts()).thenReturn(flowOf(emptyList()))

        val viewModel = HistoryViewModel(historyRepository, getWorkoutsUseCase, deleteWorkoutUseCase)
        advanceUntilIdle()

        viewModel.setFilter("Unknown Mode")
        advanceUntilIdle()

        assertEquals("Unknown Mode", viewModel.uiState.value.selectedFilter)
        verify(historyRepository).getHistoryByMode("")
    }

    @Test
    fun `clearHistory deletes all history`() = runTest(testDispatcher) {
        whenever(historyRepository.getAllHistory()).thenReturn(flowOf(emptyList()))
        whenever(getWorkoutsUseCase.getAllWorkouts()).thenReturn(flowOf(emptyList()))

        val viewModel = HistoryViewModel(historyRepository, getWorkoutsUseCase, deleteWorkoutUseCase)
        advanceUntilIdle()

        viewModel.clearHistory()
        advanceUntilIdle()

        verify(historyRepository).clearAllHistory()
    }

    @Test
    fun `deleteWorkout calls use case with id`() = runTest(testDispatcher) {
        whenever(historyRepository.getAllHistory()).thenReturn(flowOf(emptyList()))
        whenever(getWorkoutsUseCase.getAllWorkouts()).thenReturn(flowOf(emptyList()))

        val viewModel = HistoryViewModel(historyRepository, getWorkoutsUseCase, deleteWorkoutUseCase)
        advanceUntilIdle()

        viewModel.deleteWorkout(42L)
        advanceUntilIdle()

        verify(deleteWorkoutUseCase).invoke(42L)
    }

    @Test
    fun `filterOptions list is correct`() = runTest(testDispatcher) {
        whenever(historyRepository.getAllHistory()).thenReturn(flowOf(emptyList()))
        whenever(getWorkoutsUseCase.getAllWorkouts()).thenReturn(flowOf(emptyList()))

        val viewModel = HistoryViewModel(historyRepository, getWorkoutsUseCase, deleteWorkoutUseCase)

        val expected = listOf("All") + TimerMode.entries.map { it.displayName }
        assertEquals(expected, viewModel.filterOptions)
    }

    @Test
    fun `savedWorkouts is exposed in uiState`() = runTest(testDispatcher) {
        whenever(historyRepository.getAllHistory()).thenReturn(flowOf(emptyList()))
        whenever(getWorkoutsUseCase.getAllWorkouts()).thenReturn(flowOf(emptyList()))

        val viewModel = HistoryViewModel(historyRepository, getWorkoutsUseCase, deleteWorkoutUseCase)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.savedWorkouts.isEmpty())
    }
}