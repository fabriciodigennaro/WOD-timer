package com.wodtimer.app.data.repository

import com.wodtimer.app.data.local.dao.WorkoutDao
import com.wodtimer.app.data.local.entity.WorkoutEntity
import com.wodtimer.app.domain.model.BlockType
import com.wodtimer.app.domain.model.TimerBlock
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.Workout
import com.wodtimer.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override fun getAllWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavoriteWorkouts(): Flow<List<Workout>> {
        return workoutDao.getFavoriteWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getWorkoutById(id: Long): Workout? {
        return workoutDao.getWorkoutById(id)?.toDomain()
    }

    override suspend fun saveWorkout(workout: Workout): Long {
        return workoutDao.insertWorkout(workout.toEntity())
    }

    override suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout.toEntity())
    }

    override suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout.toEntity())
    }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        workoutDao.toggleFavorite(id, isFavorite)
    }

    override suspend fun deleteById(id: Long) {
        workoutDao.deleteById(id)
    }

    private fun WorkoutEntity.toDomain(): Workout {
        return Workout(
            id = id,
            title = title,
            description = description,
            mode = try { TimerMode.valueOf(mode) } catch (e: Exception) { TimerMode.FOR_TIME },
            wodText = wodText,
            isFavorite = isFavorite,
            createdAt = createdAt,
            updatedAt = updatedAt,
            durationSeconds = durationSeconds,
            rounds = rounds,
            blocks = parseBlocks(blocksJson)
        )
    }

    private fun Workout.toEntity(): WorkoutEntity {
        return WorkoutEntity(
            id = id,
            title = title,
            description = description,
            mode = mode.name,
            wodText = wodText,
            isFavorite = isFavorite,
            createdAt = createdAt,
            updatedAt = updatedAt,
            durationSeconds = durationSeconds,
            rounds = rounds,
            blocksJson = encodeBlocks(blocks)
        )
    }

    private fun parseBlocks(json: String): List<TimerBlock> {
        if (json.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                TimerBlock(
                    type = try { BlockType.valueOf(obj.getString("type")) } catch (e: Exception) { BlockType.WORK },
                    durationSeconds = obj.getInt("durationSeconds"),
                    label = obj.optString("label", "")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    private fun encodeBlocks(blocks: List<TimerBlock>): String {
        val arr = JSONArray()
        blocks.forEach { block ->
            val obj = JSONObject()
            obj.put("type", block.type.name)
            obj.put("durationSeconds", block.durationSeconds)
            obj.put("label", block.label)
            arr.put(obj)
        }
        return arr.toString()
    }
}
