package com.wodtimer.app.domain.usecase

import com.wodtimer.app.domain.repository.WorkoutRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(JUnit4::class)
class ToggleFavoriteUseCaseTest {

    private val repository: WorkoutRepository = mock()
    private val useCase = ToggleFavoriteUseCase(repository)

    @Test
    fun `invoke delegates`() = runTest {
        useCase(3L, true)
        verify(repository).toggleFavorite(3L, true)
    }
}