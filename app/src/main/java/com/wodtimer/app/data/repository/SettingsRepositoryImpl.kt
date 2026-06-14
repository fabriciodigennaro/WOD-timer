package com.wodtimer.app.data.repository

import com.wodtimer.app.data.local.dao.SettingsDao
import com.wodtimer.app.data.local.entity.SettingsEntity
import com.wodtimer.app.domain.model.AppSettings
import com.wodtimer.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun getSettings(): Flow<AppSettings> {
        return settingsDao.getSettings().map { entity ->
            entity?.toDomain() ?: AppSettings()
        }
    }

    override suspend fun getSettingsSync(): AppSettings {
        return settingsDao.getSettingsSync()?.toDomain() ?: AppSettings()
    }

    override suspend fun updateSettings(settings: AppSettings) {
        settingsDao.updateSettings(settings.toEntity())
    }

    override suspend fun initDefaultSettings() {
        if (settingsDao.getSettingsSync() == null) {
            settingsDao.insertSettings(SettingsEntity())
        }
    }

    private fun SettingsEntity.toDomain() = AppSettings(
        soundEnabled = soundEnabled,
        vibrationEnabled = vibrationEnabled,
        ttsEnabled = ttsEnabled,
        prepareCountdown = prepareCountdown,
        darkTheme = darkTheme,
        keepScreenAwake = keepScreenAwake,
        soundVolume = soundVolume,
        beepAtEachMinute = beepAtEachMinute,
        lastBeepWarning = lastBeepWarning
    )

    private fun AppSettings.toEntity() = SettingsEntity(
        id = 1,
        soundEnabled = soundEnabled,
        vibrationEnabled = vibrationEnabled,
        ttsEnabled = ttsEnabled,
        prepareCountdown = prepareCountdown,
        darkTheme = darkTheme,
        keepScreenAwake = keepScreenAwake,
        soundVolume = soundVolume,
        beepAtEachMinute = beepAtEachMinute,
        lastBeepWarning = lastBeepWarning
    )
}
