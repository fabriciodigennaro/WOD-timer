package com.wodtimer.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wodtimer.app.data.local.dao.SettingsDao
import com.wodtimer.app.data.local.dao.WorkoutDao
import com.wodtimer.app.data.local.dao.WorkoutHistoryDao
import com.wodtimer.app.data.local.entity.SettingsEntity
import com.wodtimer.app.data.local.entity.WorkoutEntity
import com.wodtimer.app.data.local.entity.WorkoutHistoryEntity

@Database(
    entities = [
        WorkoutEntity::class,
        WorkoutHistoryEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
    abstract fun settingsDao(): SettingsDao
}
