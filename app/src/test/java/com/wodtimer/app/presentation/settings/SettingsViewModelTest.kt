package com.wodtimer.app.presentation.settings

import com.wodtimer.app.domain.model.AppSettings
import com.wodtimer.app.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsRepository: SettingsRepository = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(settingsRepository.getSettings()).thenReturn(flowOf(AppSettings()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleSound flips soundEnabled and persists`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        val original = viewModel.settings.value.soundEnabled
        viewModel.toggleSound()
        advanceUntilIdle()

        assertEquals(!original, viewModel.settings.value.soundEnabled)
        verify(settingsRepository).updateSettings(any())
    }

    @Test
    fun `toggleVibration flips vibrationEnabled and persists`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        val original = viewModel.settings.value.vibrationEnabled
        viewModel.toggleVibration()
        advanceUntilIdle()

        assertEquals(!original, viewModel.settings.value.vibrationEnabled)
        verify(settingsRepository).updateSettings(any())
    }

    @Test
    fun `toggleTts flips ttsEnabled and persists`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        val original = viewModel.settings.value.ttsEnabled
        viewModel.toggleTts()
        advanceUntilIdle()

        assertEquals(!original, viewModel.settings.value.ttsEnabled)
        verify(settingsRepository).updateSettings(any())
    }

    @Test
    fun `toggleKeepAwake flips keepScreenAwake and persists`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        val original = viewModel.settings.value.keepScreenAwake
        viewModel.toggleKeepAwake()
        advanceUntilIdle()

        assertEquals(!original, viewModel.settings.value.keepScreenAwake)
        verify(settingsRepository).updateSettings(any())
    }

    @Test
    fun `toggleBeepEachMinute flips beepAtEachMinute and persists`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        val original = viewModel.settings.value.beepAtEachMinute
        viewModel.toggleBeepEachMinute()
        advanceUntilIdle()

        assertEquals(!original, viewModel.settings.value.beepAtEachMinute)
        verify(settingsRepository).updateSettings(any())
    }

    @Test
    fun `toggleLastBeepWarning flips lastBeepWarning and persists`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        val original = viewModel.settings.value.lastBeepWarning
        viewModel.toggleLastBeepWarning()
        advanceUntilIdle()

        assertEquals(!original, viewModel.settings.value.lastBeepWarning)
        verify(settingsRepository).updateSettings(any())
    }

    @Test
    fun `setPrepareCountdown updates prepareCountdown and persists`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        viewModel.setPrepareCountdown(15)
        advanceUntilIdle()

        assertEquals(15, viewModel.settings.value.prepareCountdown)
        verify(settingsRepository).updateSettings(any())
    }

    @Test
    fun `setVolume updates soundVolume and persists`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        viewModel.setVolume(0.75f)
        advanceUntilIdle()

        assertEquals(0.75f, viewModel.settings.value.soundVolume, 0.01f)
        verify(settingsRepository).updateSettings(any())
    }
}