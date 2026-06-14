package com.wodtimer.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.Workout
import com.wodtimer.app.domain.repository.SettingsRepository
import com.wodtimer.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentWorkouts: List<Workout> = emptyList(),
    val favoriteWorkouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.initDefaultSettings()
        }

        viewModelScope.launch {
            combine(
                workoutRepository.getAllWorkouts(),
                workoutRepository.getFavoriteWorkouts()
            ) { all, favorites ->
                HomeUiState(
                    recentWorkouts = all.take(10),
                    favoriteWorkouts = favorites.take(10),
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onTimerSelected(mode: TimerMode) {
        // Navigation handled by the composable
    }

    val timerModes = listOf(
        TimerMode.FOR_TIME,
        TimerMode.EMOM,
        TimerMode.TABATA,
        TimerMode.AMRAP,
        TimerMode.INTERVAL,
        TimerMode.CUSTOM
    )
}
