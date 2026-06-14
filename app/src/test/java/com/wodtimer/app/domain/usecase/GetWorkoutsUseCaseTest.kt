package com.wodtimer.app.domain.usecase

import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.Workout
import com.wodtimer.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(JUnit4::class)
class GetWorkoutsUseCaseTest {

    private val repository: WorkoutRepository = mock()
    private val useCase = GetWorkoutsUseCase(repository)

    @Test
    fun `getAllWorkouts delegates`() = runTest {
        val workouts = listOf(Workout(title = "A", mode = TimerMode.AMRAP))
        whenever(repository.getAllWorkouts()).thenReturn(flowOf(workouts))
        assertEquals(workouts, useCase.getAllWorkouts().first())
    }

    @Test
    fun `getFavoriteWorkouts delegates`() = runTest {
        val workouts = listOf(Workout(title = "B", mode = TimerMode.FOR_TIME))
        whenever(repository.getFavoriteWorkouts()).thenReturn(flowOf(workouts))
        assertEquals(workouts, useCase.getFavoriteWorkouts().first())
    }
}