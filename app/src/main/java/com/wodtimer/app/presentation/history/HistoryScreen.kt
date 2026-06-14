package com.wodtimer.app.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.WorkoutHistory
import com.wodtimer.app.presentation.theme.*
import com.wodtimer.app.util.TimeFormatter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWorkout: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isHistoryEmpty = uiState.historyList.isEmpty() && !uiState.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TimerWhite)
                    }
                },
                actions = {
                    if (uiState.historyList.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear", tint = TimerDim)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TimerWhite
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.filterOptions) { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ButtonPrimaryContainer,
                            selectedLabelColor = Green,
                            containerColor = CardBackground,
                            labelColor = TimerDim
                        )
                    )
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
            } else if (isHistoryEmpty && uiState.savedWorkouts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = null,
                            tint = TimerDim,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No workout history yet", color = TimerDim, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // History section
                    if (uiState.historyList.isNotEmpty()) {
                        item(key = "history_header") {
                            Text(
                                "Recent Activity",
                                color = TimerWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(uiState.historyList, key = { "history_${it.id}" }) { entry ->
                            HistoryCard(entry = entry)
                        }
                    }

                    // Saved Workouts section
                    if (uiState.savedWorkouts.isNotEmpty()) {
                        item(key = "saved_header") {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Saved Workouts",
                                color = TimerWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(uiState.savedWorkouts, key = { "saved_${it.id}" }) { workout ->
                            SavedWorkoutCard(
                                workout = workout,
                                onClick = { onNavigateToWorkout(workout.id) },
                                onDelete = { viewModel.deleteWorkout(workout.id) }
                            )
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(entry: WorkoutHistory) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.workoutTitle.ifBlank { entry.mode.displayName },
                    color = TimerWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = entry.mode.displayName,
                    color = Green,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Time: ${TimeFormatter.formatMillis(entry.elapsedMillis)}",
                    color = TimerDim,
                    fontSize = 14.sp
                )
                if (entry.roundsCompleted > 0) {
                    Text(
                        text = "Rounds: ${entry.roundsCompleted}",
                        color = TimerDim,
                        fontSize = 14.sp
                    )
                }
            }
            if (entry.wodText.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = entry.wodText,
                    color = TimerDim,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = dateFormat.format(Date(entry.completedAt)),
                color = TimerDim,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SavedWorkoutCard(
    workout: com.wodtimer.app.domain.model.Workout,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = workout.title.ifBlank { "Untitled" },
                        color = TimerWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (workout.isFavorite) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Yellow, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = workout.mode.displayName,
                    color = Green,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = TimerDim)
            }
        }
    }
}
