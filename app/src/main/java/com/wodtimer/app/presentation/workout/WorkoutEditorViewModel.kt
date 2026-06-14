package com.wodtimer.app.presentation.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wodtimer.app.domain.model.TimerBlock
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.Workout
import com.wodtimer.app.domain.usecase.SaveWorkoutUseCase
import com.wodtimer.app.domain.usecase.GetWorkoutsUseCase
import com.wodtimer.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutEditorUiState(
    val title: String = "",
    val description: String = "",
    val mode: TimerMode = TimerMode.FOR_TIME,
    val wodText: String = "",
    val durationSeconds: Int = 0,
    val rounds: Int = 0,
    val blocks: List<TimerBlock> = emptyList(),
    val isSaved: Boolean = false,
    val isExisting: Boolean = false
)

@HiltViewModel
class WorkoutEditorViewModel @Inject constructor(
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val getWorkoutsUseCase: GetWorkoutsUseCase,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutEditorUiState())
    val uiState: StateFlow<WorkoutEditorUiState> = _uiState.asStateFlow()

    fun loadWorkout(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutById(id) ?: return@launch
            _uiState.value = WorkoutEditorUiState(
                title = workout.title,
                description = workout.description,
                mode = workout.mode,
                wodText = workout.wodText,
                durationSeconds = workout.durationSeconds,
                rounds = workout.rounds,
                blocks = workout.blocks,
                isExisting = true
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun updateMode(mode: TimerMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    fun updateWodText(text: String) {
        _uiState.value = _uiState.value.copy(wodText = text)
    }

    fun updateDuration(seconds: Int) {
        _uiState.value = _uiState.value.copy(durationSeconds = seconds)
    }

    fun updateRounds(rounds: Int) {
        _uiState.value = _uiState.value.copy(rounds = rounds)
    }

    fun saveWorkout(onSaved: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val workout = Workout(
                title = state.title.ifBlank { "Untitled WOD" },
                description = state.description,
                mode = state.mode,
                wodText = state.wodText,
                durationSeconds = state.durationSeconds,
                rounds = state.rounds,
                blocks = state.blocks
            )
            saveWorkoutUseCase(workout)
            _uiState.value = _uiState.value.copy(isSaved = true)
            onSaved()
        }
    }

    val modes = TimerMode.entries
}
