package com.wodtimer.app.presentation.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.Workout
import com.wodtimer.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTimer: (String, Long) -> Unit,
    onNavigateToWorkoutEditor: (Long) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "WODTimer",
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Outlined.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Timer Modes Grid
            TimerModesGrid(
                modes = viewModel.timerModes,
                onModeSelected = { onNavigateToTimer(it.name, -1L) }
            )

            // Recent WODs
            if (uiState.recentWorkouts.isNotEmpty()) {
                SectionHeader(
                    title = "Recent WODs",
                    onSeeAll = onNavigateToHistory
                )
                RecentWodsRow(
                    workouts = uiState.recentWorkouts,
                    onWorkoutClick = { onNavigateToWorkoutEditor(it.id) },
                    onStartTimer = { onNavigateToTimer(it.mode.name, it.id) }
                )
            }

            // Favorites
            if (uiState.favoriteWorkouts.isNotEmpty()) {
                SectionHeader(
                    title = "Favorites",
                    onSeeAll = onNavigateToHistory
                )
                RecentWodsRow(
                    workouts = uiState.favoriteWorkouts,
                    onWorkoutClick = { onNavigateToWorkoutEditor(it.id) },
                    onStartTimer = { onNavigateToTimer(it.mode.name, it.id) }
                )
            }

            // Quick Create
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { onNavigateToWorkoutEditor(-1L) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonPrimaryContainer,
                    contentColor = Green
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create Custom WOD", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimerModesGrid(
    modes: List<TimerMode>,
    onModeSelected: (TimerMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        modes.chunked(2).forEach { rowModes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowModes.forEach { mode ->
                    TimerModeCard(
                        mode = mode,
                        onClick = { onModeSelected(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowModes.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TimerModeCard(
    mode: TimerMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color) = getModeMeta(mode)

    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            DarkSurface,
                            DarkSurfaceVariant
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = mode.displayName,
                    color = TimerWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TimerWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        TextButton(onClick = onSeeAll) {
            Text("See All", color = Green)
        }
    }
}

@Composable
private fun RecentWodsRow(
    workouts: List<Workout>,
    onWorkoutClick: (Workout) -> Unit,
    onStartTimer: (Workout) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(workouts) { workout ->
            WodCard(
                workout = workout,
                onClick = { onWorkoutClick(workout) },
                onPlay = { onStartTimer(workout) }
            )
        }
    }
}

@Composable
private fun WodCard(
    workout: Workout,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workout.mode.displayName,
                    color = Green,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                if (workout.isFavorite) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Yellow,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = workout.title.ifBlank { "Untitled" },
                color = TimerWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (workout.wodText.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = workout.wodText.take(60),
                    color = TimerDim,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledIconButton(
                    onClick = onPlay,
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Green,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun getModeMeta(mode: TimerMode): Pair<ImageVector, Color> {
    return when (mode) {
        TimerMode.FOR_TIME -> Icons.Outlined.Timer to TimerRed
        TimerMode.EMOM -> Icons.Outlined.Schedule to TimerYellow
        TimerMode.TABATA -> Icons.Outlined.FitnessCenter to Green
        TimerMode.AMRAP -> Icons.Outlined.Repeat to Blue
        TimerMode.INTERVAL -> Icons.Outlined.Timeline to Orange
        TimerMode.CUSTOM -> Icons.Outlined.Build to TimerWhite
    }
}
