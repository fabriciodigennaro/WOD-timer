package com.wodtimer.app.data.repository

import com.wodtimer.app.data.local.dao.SettingsDao
import com.wodtimer.app.data.local.entity.SettingsEntity
import com.wodtimer.app.domain.model.AppSettings
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(JUnit4::class)
class SettingsRepositoryImplTest {

    private val dao: SettingsDao = mock()
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        repository = SettingsRepositoryImpl(dao)
    }

    @Test
    fun `getSettings maps entity to domain`() = runTest {
        val entity = SettingsEntity(
            soundEnabled = false,
            vibrationEnabled = false,
            ttsEnabled = false,
            prepareCountdown = 10,
            darkTheme = false,
            keepScreenAwake = false,
            soundVolume = 0.5f,
            beepAtEachMinute = false,
            lastBeepWarning = false
        )
        whenever(dao.getSettings()).thenReturn(flowOf(entity))

        val result = repository.getSettings().first()

        assert(
            result == AppSettings(
                soundEnabled = false,
                vibrationEnabled = false,
                ttsEnabled = false,
                prepareCountdown = 10,
                darkTheme = false,
                keepScreenAwake = false,
                soundVolume = 0.5f,
                beepAtEachMinute = false,
                lastBeepWarning = false
            )
        )
    }

    @Test
    fun `getSettings returns defaults when entity is null`() = runTest {
        whenever(dao.getSettings()).thenReturn(flowOf(null))

        val result = repository.getSettings().first()

        assert(result == AppSettings())
    }

    @Test
    fun `getSettingsSync maps entity to domain`() = runTest {
        val entity = SettingsEntity(
            soundEnabled = false,
            prepareCountdown = 15
        )
        whenever(dao.getSettingsSync()).thenReturn(entity)

        val result = repository.getSettingsSync()

        assert(result == AppSettings(soundEnabled = false, prepareCountdown = 15))
    }

    @Test
    fun `getSettingsSync returns defaults when entity is null`() = runTest {
        whenever(dao.getSettingsSync()).thenReturn(null)

        val result = repository.getSettingsSync()

        assert(result == AppSettings())
    }

    @Test
    fun `updateSettings converts to entity and calls dao`() = runTest {
        val settings = AppSettings(
            soundEnabled = false,
            vibrationEnabled = false,
            prepareCountdown = 12
        )
        val expectedEntity = SettingsEntity(
            id = 1,
            soundEnabled = false,
            vibrationEnabled = false,
            prepareCountdown = 12
        )

        repository.updateSettings(settings)

        verify(dao).updateSettings(expectedEntity)
    }

    @Test
    fun `initDefaultSettings inserts when no settings exist`() = runTest {
        whenever(dao.getSettingsSync()).thenReturn(null)

        repository.initDefaultSettings()

        verify(dao).insertSettings(SettingsEntity())
    }

    @Test
    fun `initDefaultSettings does not insert when settings already exist`() = runTest {
        whenever(dao.getSettingsSync()).thenReturn(SettingsEntity())

        repository.initDefaultSettings()

        verify(dao, never()).insertSettings(any())
    }
}
