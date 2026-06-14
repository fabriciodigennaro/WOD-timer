package com.wodtimer.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
    val prepareCountdown: Int = 5,
    val darkTheme: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val soundVolume: Float = 0.8f,
    val beepAtEachMinute: Boolean = true,
    val lastBeepWarning: Boolean = true
)
