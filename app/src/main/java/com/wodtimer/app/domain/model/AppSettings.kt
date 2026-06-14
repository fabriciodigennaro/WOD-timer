package com.wodtimer.app.domain.model

data class AppSettings(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
    val prepareCountdown: Int = 10,
    val darkTheme: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val soundVolume: Float = 0.8f,
    val beepAtEachMinute: Boolean = true,
    val lastBeepWarning: Boolean = true
)
