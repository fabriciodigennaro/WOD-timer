package com.wodtimer.app.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditorScreen(
    workoutId: Long,
    onNavigateBack: () -> Unit,
    viewModel: WorkoutEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isExisting) "Edit WOD" else "Create WOD",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TimerWhite)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveWorkout {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Text("Save", color = Green, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("WOD Title") },
                placeholder = { Text("e.g. \"Murph\"", color = TimerDim) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TimerWhite,
                    unfocusedTextColor = TimerWhite,
                    cursorColor = Green,
                    focusedBorderColor = Green,
                    unfocusedBorderColor = DividerColor,
                    focusedLabelColor = Green,
                    unfocusedLabelColor = TimerDim
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TimerWhite,
                    unfocusedTextColor = TimerWhite,
                    cursorColor = Green,
                    focusedBorderColor = Green,
                    unfocusedBorderColor = DividerColor,
                    focusedLabelColor = Green,
                    unfocusedLabelColor = TimerDim
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Timer Mode selector
            Text("Timer Mode", color = TimerWhite, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            ModeRow(
                modes = viewModel.modes,
                selectedMode = uiState.mode,
                onModeSelected = { viewModel.updateMode(it) }
            )

            // WOD Details
            Text("WOD Details", color = TimerWhite, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(CardBackground, RoundedCornerShape(12.dp))
            ) {
                BasicTextField(
                    value = uiState.wodText,
                    onValueChange = { viewModel.updateWodText(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    textStyle = TextStyle(
                        color = TimerWhite,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(Green),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.wodText.isEmpty()) {
                                Text(
                                    text = "EMOM 12\n1: 10 Burpees\n2: 15 Wall Balls\n3: Rest\n\nFOR TIME\n21-15-9\nThrusters\nPull Ups",
                                    color = TimerDim,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // Duration (minutes)
            Text("Duration (minutes)", color = TimerWhite, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            OutlinedTextField(
                value = if (uiState.durationSeconds > 0) (uiState.durationSeconds / 60).toString() else "",
                onValueChange = {
                    val min = it.toIntOrNull()?.coerceIn(0, 999) ?: 0
                    viewModel.updateDuration(min * 60)
                },
                label = { Text("Minutes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TimerWhite,
                    unfocusedTextColor = TimerWhite,
                    cursorColor = Green,
                    focusedBorderColor = Green,
                    unfocusedBorderColor = DividerColor,
                    focusedLabelColor = Green,
                    unfocusedLabelColor = TimerDim
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Rounds
            Text("Rounds", color = TimerWhite, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            OutlinedTextField(
                value = if (uiState.rounds > 0) uiState.rounds.toString() else "",
                onValueChange = {
                    viewModel.updateRounds(it.toIntOrNull()?.coerceIn(0, 999) ?: 0)
                },
                label = { Text("Number of rounds") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TimerWhite,
                    unfocusedTextColor = TimerWhite,
                    cursorColor = Green,
                    focusedBorderColor = Green,
                    unfocusedBorderColor = DividerColor,
                    focusedLabelColor = Green,
                    unfocusedLabelColor = TimerDim
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModeRow(
    modes: List<TimerMode>,
    selectedMode: TimerMode,
    onModeSelected: (TimerMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        modes.chunked(3).forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunk.forEach { mode ->
                    ModeChip(
                        mode = mode,
                        isSelected = selectedMode == mode,
                        onClick = { onModeSelected(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (chunk.size < 3) {
                    repeat(3 - chunk.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeChip(
    mode: TimerMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ButtonPrimaryContainer else CardBackground
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mode.displayName,
                color = if (isSelected) Green else TimerWhite,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )
        }
    }
}
