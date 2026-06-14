package com.wodtimer.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wodtimer.app.domain.model.AppSettings
import com.wodtimer.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect {
                _settings.value = it
            }
        }
    }

    fun toggleSound() {
        updateSettings(_settings.value.copy(soundEnabled = !_settings.value.soundEnabled))
    }

    fun toggleVibration() {
        updateSettings(_settings.value.copy(vibrationEnabled = !_settings.value.vibrationEnabled))
    }

    fun toggleTts() {
        updateSettings(_settings.value.copy(ttsEnabled = !_settings.value.ttsEnabled))
    }

    fun toggleKeepAwake() {
        updateSettings(_settings.value.copy(keepScreenAwake = !_settings.value.keepScreenAwake))
    }

    fun toggleBeepEachMinute() {
        updateSettings(_settings.value.copy(beepAtEachMinute = !_settings.value.beepAtEachMinute))
    }

    fun toggleLastBeepWarning() {
        updateSettings(_settings.value.copy(lastBeepWarning = !_settings.value.lastBeepWarning))
    }

    fun setPrepareCountdown(seconds: Int) {
        updateSettings(_settings.value.copy(prepareCountdown = seconds))
    }

    fun setVolume(volume: Float) {
        updateSettings(_settings.value.copy(soundVolume = volume))
    }

    private fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
        }
    }
}
