package com.wodtimer.app.di

import android.content.Context
import androidx.room.Room
import com.wodtimer.app.data.local.AppDatabase
import com.wodtimer.app.data.local.dao.SettingsDao
import com.wodtimer.app.data.local.dao.WorkoutDao
import com.wodtimer.app.data.local.dao.WorkoutHistoryDao
import com.wodtimer.app.data.repository.SettingsRepositoryImpl
import com.wodtimer.app.data.repository.WorkoutHistoryRepositoryImpl
import com.wodtimer.app.data.repository.WorkoutRepositoryImpl
import com.wodtimer.app.domain.repository.SettingsRepository
import com.wodtimer.app.domain.repository.WorkoutHistoryRepository
import com.wodtimer.app.domain.repository.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "wodtimer_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideWorkoutHistoryDao(db: AppDatabase): WorkoutHistoryDao = db.workoutHistoryDao()

    @Provides
    fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()

    @Provides
    @Singleton
    fun provideWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository = impl

    @Provides
    @Singleton
    fun provideWorkoutHistoryRepository(impl: WorkoutHistoryRepositoryImpl): WorkoutHistoryRepository = impl

    @Provides
    @Singleton
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl
}
