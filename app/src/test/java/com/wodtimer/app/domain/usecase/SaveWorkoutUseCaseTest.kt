package com.wodtimer.app.domain.usecase

import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.Workout
import com.wodtimer.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(JUnit4::class)
class SaveWorkoutUseCaseTest {

    private val repository: WorkoutRepository = mock()
    private val useCase = SaveWorkoutUseCase(repository)

    @Test
    fun `invoke delegates and returns id`() = runTest {
        whenever(repository.saveWorkout(any())).thenReturn(7L)
        val result = useCase(Workout(title = "Test", mode = TimerMode.TABATA))
        assertEquals(7L, result)
    }
}