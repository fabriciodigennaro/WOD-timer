package com.wodtimer.app.data.repository

import com.wodtimer.app.data.local.dao.WorkoutHistoryDao
import com.wodtimer.app.data.local.entity.WorkoutHistoryEntity
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.WorkoutHistory
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(JUnit4::class)
class WorkoutHistoryRepositoryImplTest {

    private val dao: WorkoutHistoryDao = mock()
    private lateinit var repository: WorkoutHistoryRepositoryImpl

    @Before
    fun setUp() {
        repository = WorkoutHistoryRepositoryImpl(dao)
    }

    @Test
    fun `getAllHistory maps entities to domain`() = runTest {
        val entity = WorkoutHistoryEntity(
            id = 1,
            workoutTitle = "WOD",
            mode = "AMRAP",
            elapsedMillis = 300000L,
            roundsCompleted = 5,
            notes = "Good session",
            completedAt = 1000L,
            isFavorite = true
        )
        whenever(dao.getAllHistory()).thenReturn(flowOf(listOf(entity)))

        val result = repository.getAllHistory().first()

        assert(result.size == 1)
        with(result[0]) {
            assert(id == 1L)
            assert(workoutTitle == "WOD")
            assert(mode == TimerMode.AMRAP)
            assert(elapsedMillis == 300000L)
            assert(roundsCompleted == 5)
            assert(notes == "Good session")
            assert(completedAt == 1000L)
            assert(isFavorite)
        }
    }

    @Test
    fun `getAllHistory returns empty list`() = runTest {
        whenever(dao.getAllHistory()).thenReturn(flowOf(emptyList()))

        val result = repository.getAllHistory().first()

        assert(result.isEmpty())
    }

    @Test
    fun `getHistoryByMode delegates to dao with mode string`() = runTest {
        val entity = WorkoutHistoryEntity(id = 1, workoutTitle = "EMOM WOD", mode = "EMOM")
        whenever(dao.getHistoryByMode("EMOM")).thenReturn(flowOf(listOf(entity)))

        val result = repository.getHistoryByMode("EMOM").first()

        assert(result.size == 1)
        assert(result[0].mode == TimerMode.EMOM)
    }

    @Test
    fun `getHistoryByMode returns empty list for mode with no history`() = runTest {
        whenever(dao.getHistoryByMode("TABATA")).thenReturn(flowOf(emptyList()))

        val result = repository.getHistoryByMode("TABATA").first()

        assert(result.isEmpty())
    }

    @Test
    fun `insertHistory converts to entity and returns id`() = runTest {
        val history = WorkoutHistory(
            workoutTitle = "New History",
            mode = TimerMode.FOR_TIME,
            elapsedMillis = 120000L,
            roundsCompleted = 3,
            notes = "OK",
            completedAt = 5000L,
            isFavorite = false
        )
        val expectedEntity = WorkoutHistoryEntity(
            workoutTitle = "New History",
            mode = "FOR_TIME",
            elapsedMillis = 120000L,
            roundsCompleted = 3,
            notes = "OK",
            completedAt = 5000L,
            isFavorite = false
        )
        whenever(dao.insertHistory(any())).thenReturn(99L)

        val result = repository.insertHistory(history)

        assert(result == 99L)
        verify(dao).insertHistory(expectedEntity)
    }

    @Test
    fun `deleteHistory converts to entity and calls dao`() = runTest {
        val history = WorkoutHistory(
            id = 5,
            workoutTitle = "Delete Me",
            mode = TimerMode.INTERVAL,
            elapsedMillis = 60000L,
            completedAt = 100L
        )
        val expectedEntity = WorkoutHistoryEntity(
            id = 5,
            workoutTitle = "Delete Me",
            mode = "INTERVAL",
            elapsedMillis = 60000L,
            completedAt = 100L
        )

        repository.deleteHistory(history)

        verify(dao).deleteHistory(expectedEntity)
    }

    @Test
    fun `clearAllHistory delegates to dao`() = runTest {
        repository.clearAllHistory()

        verify(dao).clearAllHistory()
    }

    @Test
    fun `entity mapping falls back to FOR_TIME for invalid mode string`() = runTest {
        val entity = WorkoutHistoryEntity(
            id = 1,
            workoutTitle = "Bad Mode",
            mode = "INVALID_MODE"
        )
        whenever(dao.getAllHistory()).thenReturn(flowOf(listOf(entity)))

        val result = repository.getAllHistory().first()

        assert(result[0].mode == TimerMode.FOR_TIME)
    }

    @Test
    fun `entity mapping handles all valid timer modes`() = runTest {
        val entities = TimerMode.entries.map { mode ->
            WorkoutHistoryEntity(
                id = mode.ordinal.toLong(),
                workoutTitle = mode.name,
                mode = mode.name
            )
        }
        whenever(dao.getAllHistory()).thenReturn(flowOf(entities))

        val result = repository.getAllHistory().first()

        TimerMode.entries.forEach { mode ->
            val item = result.find { it.id == mode.ordinal.toLong() }
            assert(item != null) { "Missing history for mode ${mode.name}" }
            assert(item!!.mode == mode) { "Expected ${mode.name} but got ${item.mode}" }
        }
    }
}
