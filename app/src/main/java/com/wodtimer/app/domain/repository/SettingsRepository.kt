package com.wodtimer.app.domain.repository

import com.wodtimer.app.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun getSettingsSync(): AppSettings
    suspend fun updateSettings(settings: AppSettings)
    suspend fun initDefaultSettings()
}
