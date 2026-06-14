package com.wodtimer.app.data.repository

import com.wodtimer.app.data.local.dao.WorkoutDao
import com.wodtimer.app.data.local.entity.WorkoutEntity
import com.wodtimer.app.domain.model.BlockType
import com.wodtimer.app.domain.model.TimerBlock
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.Workout
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(JUnit4::class)
class WorkoutRepositoryImplTest {

    private val dao: WorkoutDao = mock()
    private lateinit var repository: WorkoutRepositoryImpl

    @Before
    fun setUp() {
        repository = WorkoutRepositoryImpl(dao)
    }

    @Test
    fun `getAllWorkouts maps entities`() = runTest {
        val entity = WorkoutEntity(id = 1, title = "Test", mode = "AMRAP")
        whenever(dao.getAllWorkouts()).thenReturn(flowOf(listOf(entity)))
        val result = repository.getAllWorkouts().first()
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(TimerMode.AMRAP, result[0].mode)
    }

    @Test
    fun `getAllWorkouts empty`() = runTest {
        whenever(dao.getAllWorkouts()).thenReturn(flowOf(emptyList()))
        assertTrue(repository.getAllWorkouts().first().isEmpty())
    }

    @Test
    fun `getFavoriteWorkouts maps`() = runTest {
        val entity = WorkoutEntity(id = 2, title = "Fav", isFavorite = true, mode = "FOR_TIME")
        whenever(dao.getFavoriteWorkouts()).thenReturn(flowOf(listOf(entity)))
        val result = repository.getFavoriteWorkouts().first()
        assertEquals(1, result.size)
        assertTrue(result[0].isFavorite)
    }

    @Test
    fun `getWorkoutById returns mapped`() = runTest {
        whenever(dao.getWorkoutById(3)).thenReturn(WorkoutEntity(id = 3, title = "Test", mode = "EMOM"))
        val result = repository.getWorkoutById(3)
        assertNotNull(result)
        assertEquals(TimerMode.EMOM, result!!.mode)
    }

    @Test
    fun `getWorkoutById returns null`() = runTest {
        whenever(dao.getWorkoutById(99)).thenReturn(null)
        assertNull(repository.getWorkoutById(99))
    }

    @Test
    fun `saveWorkout returns dao result`() = runTest {
        whenever(dao.insertWorkout(any())).thenReturn(42L)
        val id = repository.saveWorkout(Workout(title = "New", mode = TimerMode.TABATA))
        assertEquals(42L, id)
    }

    @Test
    fun `updateWorkout delegates`() = runTest {
        repository.updateWorkout(Workout(id = 1, title = "Updated", mode = TimerMode.AMRAP))
    }

    @Test
    fun `deleteWorkout delegates`() = runTest {
        repository.deleteWorkout(Workout(id = 1, title = "Del", mode = TimerMode.FOR_TIME))
    }

    @Test
    fun `parseBlocks returns empty for blank`() = runTest {
        whenever(dao.getAllWorkouts()).thenReturn(
            flowOf(listOf(WorkoutEntity(id = 1, title = "T", mode = "FOR_TIME", blocksJson = ""))))
        assertTrue(repository.getAllWorkouts().first()[0].blocks.isEmpty())
    }

    @Test
    fun `parseBlocks returns empty for malformed`() = runTest {
        whenever(dao.getAllWorkouts()).thenReturn(
            flowOf(listOf(WorkoutEntity(id = 1, title = "T", mode = "FOR_TIME", blocksJson = "bad"))))
        assertTrue(repository.getAllWorkouts().first()[0].blocks.isEmpty())
    }

    @Test
    fun `entity mapping falls back to FOR_TIME`() = runTest {
        whenever(dao.getAllWorkouts()).thenReturn(
            flowOf(listOf(WorkoutEntity(id = 1, title = "T", mode = "BAD"))))
        assertEquals(TimerMode.FOR_TIME, repository.getAllWorkouts().first()[0].mode)
    }

    @Test
    fun `toggleFavorite delegates`() = runTest {
        repository.toggleFavorite(10L, true)
        verify(dao).toggleFavorite(10L, true)
    }

    @Test
    fun `deleteById delegates`() = runTest {
        repository.deleteById(20L)
        verify(dao).deleteById(20L)
    }

    @Test
    fun `parseBlocks parses valid JSON with multiple blocks`() = runTest {
        val json = """[{"type":"WARMUP","durationSeconds":60,"label":"Warmup"},{"type":"WORK","durationSeconds":300,"label":"Main Work"},{"type":"COOLDOWN","durationSeconds":120,"label":"Cool Down"}]"""
        whenever(dao.getAllWorkouts()).thenReturn(
            flowOf(listOf(WorkoutEntity(id = 1, title = "T", mode = "FOR_TIME", blocksJson = json))))
        val blocks = repository.getAllWorkouts().first()[0].blocks
        assertEquals(3, blocks.size)
        assertEquals(BlockType.WARMUP, blocks[0].type)
        assertEquals(60, blocks[0].durationSeconds)
        assertEquals("Warmup", blocks[0].label)
        assertEquals(BlockType.WORK, blocks[1].type)
        assertEquals(300, blocks[1].durationSeconds)
        assertEquals(BlockType.COOLDOWN, blocks[2].type)
    }

    @Test
    fun `parseBlocks falls back to WORK for unknown block type`() = runTest {
        val json = """[{"type":"INVALID","durationSeconds":60,"label":"X"}]"""
        whenever(dao.getAllWorkouts()).thenReturn(
            flowOf(listOf(WorkoutEntity(id = 1, title = "T", mode = "FOR_TIME", blocksJson = json))))
        val blocks = repository.getAllWorkouts().first()[0].blocks
        assertEquals(1, blocks.size)
        assertEquals(BlockType.WORK, blocks[0].type)
    }

    @Test
    fun `saveWorkout with blocks encodes JSON`() = runTest {
        val blocks = listOf(
            TimerBlock(BlockType.WARMUP, 60, "Warmup"),
            TimerBlock(BlockType.WORK, 300, "Work"),
            TimerBlock(BlockType.REST, 30, "Rest")
        )
        repository.saveWorkout(
            Workout(title = "Test Blocks", mode = TimerMode.CUSTOM, blocks = blocks))
        verify(dao).insertWorkout(any())
    }

    @Test
    fun `updateWorkout with blocks encodes JSON`() = runTest {
        val blocks = listOf(TimerBlock(BlockType.WORK, 120, ""))
        repository.updateWorkout(
            Workout(id = 5, title = "Update", mode = TimerMode.AMRAP, blocks = blocks))
        verify(dao).updateWorkout(any())
    }

    @Test
    fun `parseBlocks handles REST and COOLDOWN types`() = runTest {
        val json = """[{"type":"REST","durationSeconds":30,"label":"Rest"},{"type":"COOLDOWN","durationSeconds":60,"label":"CD"}]"""
        whenever(dao.getAllWorkouts()).thenReturn(
            flowOf(listOf(WorkoutEntity(id = 1, title = "T", mode = "INTERVAL", blocksJson = json))))
        val blocks = repository.getAllWorkouts().first()[0].blocks
        assertEquals(2, blocks.size)
        assertEquals(BlockType.REST, blocks[0].type)
        assertEquals(BlockType.COOLDOWN, blocks[1].type)
    }
}