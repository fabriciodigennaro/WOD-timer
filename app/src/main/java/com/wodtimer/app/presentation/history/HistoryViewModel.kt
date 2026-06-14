package com.wodtimer.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.WorkoutHistory
import com.wodtimer.app.domain.repository.WorkoutHistoryRepository
import com.wodtimer.app.domain.repository.WorkoutRepository
import com.wodtimer.app.domain.usecase.DeleteWorkoutUseCase
import com.wodtimer.app.domain.usecase.GetWorkoutsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val historyList: List<WorkoutHistory> = emptyList(),
    val savedWorkouts: List<com.wodtimer.app.domain.model.Workout> = emptyList(),
    val selectedFilter: String = "All",
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: WorkoutHistoryRepository,
    private val getWorkoutsUseCase: GetWorkoutsUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val allFilterOptions = listOf("All") + TimerMode.entries.map { it.displayName }

    init {
        viewModelScope.launch {
            combine(
                historyRepository.getAllHistory(),
                getWorkoutsUseCase.getAllWorkouts()
            ) { history, workouts ->
                HistoryUiState(
                    historyList = history,
                    savedWorkouts = workouts,
                    selectedFilter = _uiState.value.selectedFilter,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        viewModelScope.launch {
            val flow = if (filter == "All") {
                historyRepository.getAllHistory()
            } else {
                val mode = TimerMode.entries.find { it.displayName == filter }
                historyRepository.getHistoryByMode(mode?.name ?: "")
            }
            flow.collect { list ->
                _uiState.value = _uiState.value.copy(historyList = list)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearAllHistory()
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch {
            deleteWorkoutUseCase(id)
        }
    }

    val filterOptions = allFilterOptions
}
