package com.wodtimer.app.presentation.home

import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.Workout
import com.wodtimer.app.domain.repository.SettingsRepository
import com.wodtimer.app.domain.repository.WorkoutRepository
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val workoutRepository: WorkoutRepository = mock()
    private val settingsRepository: SettingsRepository = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `timerModes list contains all modes`() = runTest(testDispatcher) {
        whenever(workoutRepository.getAllWorkouts()).thenReturn(flowOf(emptyList()))
        whenever(workoutRepository.getFavoriteWorkouts()).thenReturn(flowOf(emptyList()))

        val viewModel = HomeViewModel(workoutRepository, settingsRepository)
        advanceUntilIdle()

        val expected = listOf(
            TimerMode.FOR_TIME,
            TimerMode.EMOM,
            TimerMode.TABATA,
            TimerMode.AMRAP,
            TimerMode.INTERVAL,
            TimerMode.CUSTOM
        )
        assertEquals(expected, viewModel.timerModes)
    }

    @Test
    fun `default settings initialized on start`() = runTest(testDispatcher) {
        whenever(workoutRepository.getAllWorkouts()).thenReturn(flowOf(emptyList()))
        whenever(workoutRepository.getFavoriteWorkouts()).thenReturn(flowOf(emptyList()))

        HomeViewModel(workoutRepository, settingsRepository)
        advanceUntilIdle()

        verify(settingsRepository).initDefaultSettings()
    }

    @Test
    fun `ui state shows empty when no data`() = runTest(testDispatcher) {
        whenever(workoutRepository.getAllWorkouts()).thenReturn(flowOf(emptyList()))
        whenever(workoutRepository.getFavoriteWorkouts()).thenReturn(flowOf(emptyList()))

        val viewModel = HomeViewModel(workoutRepository, settingsRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.recentWorkouts.isEmpty())
        assertTrue(state.favoriteWorkouts.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun `ui state combines workouts and favorites`() = runTest(testDispatcher) {
        val workouts = listOf(
            Workout(id = 1, title = "WOD 1"),
            Workout(id = 2, title = "WOD 2")
        )
        val favorites = listOf(
            Workout(id = 1, title = "WOD 1", isFavorite = true)
        )
        whenever(workoutRepository.getAllWorkouts()).thenReturn(flowOf(workouts))
        whenever(workoutRepository.getFavoriteWorkouts()).thenReturn(flowOf(favorites))

        val viewModel = HomeViewModel(workoutRepository, settingsRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(workouts, state.recentWorkouts)
        assertEquals(favorites, state.favoriteWorkouts)
        assertFalse(state.isLoading)
    }

    @Test
    fun `ui state limits to ten items`() = runTest(testDispatcher) {
        val manyWorkouts = (1..15).map { Workout(id = it.toLong(), title = "WOD $it") }
        val manyFavorites = (1..15).map { Workout(id = it.toLong(), title = "Fav $it", isFavorite = true) }
        whenever(workoutRepository.getAllWorkouts()).thenReturn(flowOf(manyWorkouts))
        whenever(workoutRepository.getFavoriteWorkouts()).thenReturn(flowOf(manyFavorites))

        val viewModel = HomeViewModel(workoutRepository, settingsRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(10, state.recentWorkouts.size)
        assertEquals(10, state.favoriteWorkouts.size)
    }
}