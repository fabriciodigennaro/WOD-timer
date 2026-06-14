package com.wodtimer.app.presentation.timer

import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.domain.model.TimerPhase
import com.wodtimer.app.presentation.theme.*
import com.wodtimer.app.presentation.timer.components.*
import com.wodtimer.app.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    mode: String,
    workoutId: Long = -1L,
    onNavigateBack: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerMode = remember(mode) {
        try { TimerMode.valueOf(mode) } catch (e: Exception) { TimerMode.FOR_TIME }
    }

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var durationMinutes by remember { mutableIntStateOf(10) }
    var durationSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(timerMode) {
        viewModel.configure(
            mode = timerMode,
            workSeconds = when (timerMode) {
                TimerMode.TABATA -> 20
                TimerMode.INTERVAL -> 45
                else -> 20
            },
            restSeconds = when (timerMode) {
                TimerMode.TABATA -> 10
                TimerMode.INTERVAL -> 15
                else -> 10
            },
            totalMinutes = 10,
            totalRounds = when (timerMode) {
                TimerMode.TABATA -> 8
                TimerMode.EMOM -> 10
                TimerMode.INTERVAL -> 3
                else -> 0
            },
            intervalMinutes = if (timerMode == TimerMode.EMOM) 1 else 0
        )
    }

    LaunchedEffect(workoutId) {
        if (workoutId > 0) {
            viewModel.loadWorkout(workoutId)
        }
    }

    // Keep screen awake during active workout
    LaunchedEffect(state.isRunning, state.isPaused) {
        val activity = context as? android.app.Activity
        if (state.isRunning && !state.isPaused) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val backgroundColor = when (state.phase) {
        TimerPhase.PREPARE -> DarkBackground
        TimerPhase.WORK -> DarkBackground
        TimerPhase.REST -> DarkBackground.copy(alpha = 0.95f)
        TimerPhase.COOLDOWN -> DarkBackground
        TimerPhase.FINISHED -> DarkBackground
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.wodTitle.ifBlank { timerMode.displayName },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TimerWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = TimerDim)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = TimerWhite
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Timer Display
            TimerDisplay(
                elapsedMillis = state.elapsedMillis,
                totalDurationMillis = state.totalDurationMillis,
                phase = state.phase,
                modeName = timerMode.displayName
            )

            // Round indicator
            val showManual = state.mode == TimerMode.AMRAP || state.mode == TimerMode.FOR_TIME
            RoundIndicator(
                currentRound = if (showManual) state.manualRounds else state.currentRound,
                totalRounds = state.totalRounds
            )

            // Duration selector (visible when timer is not running)
            AnimatedVisibility(
                visible = !state.isRunning && !state.isPaused && !state.isFinished,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Workout Duration",
                        color = TimerDim,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = if (durationMinutes > 0) durationMinutes.toString() else "",
                            onValueChange = {
                                durationMinutes = it.toIntOrNull()?.coerceIn(0, 999) ?: 0
                            },
                            label = { Text("Min", color = TimerDim) },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = TimerWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
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
                        Spacer(Modifier.width(8.dp))
                        Text(":", color = TimerWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = if (durationSeconds > 0 || durationMinutes > 0) durationSeconds.toString().padStart(2, '0') else "",
                            onValueChange = {
                                durationSeconds = it.toIntOrNull()?.coerceIn(0, 59) ?: 0
                            },
                            label = { Text("Sec", color = TimerDim) },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = TimerWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
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
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        listOf(1, 5, 10, 20, 30).forEach { min ->
                            FilterChip(
                                selected = durationMinutes == min && durationSeconds == 0,
                                onClick = {
                                    durationMinutes = min
                                    durationSeconds = 0
                                    viewModel.configure(
                                        mode = timerMode,
                                        wodText = state.wodText,
                                        wodTitle = state.wodTitle,
                                        workSeconds = when (timerMode) {
                                            TimerMode.TABATA -> 20
                                            TimerMode.INTERVAL -> 45
                                            else -> 20
                                        },
                                        restSeconds = when (timerMode) {
                                            TimerMode.TABATA -> 10
                                            TimerMode.INTERVAL -> 15
                                            else -> 10
                                        },
                                        totalMinutes = when (timerMode) {
                                            TimerMode.FOR_TIME -> min
                                            TimerMode.AMRAP -> min
                                            TimerMode.EMOM -> min
                                            else -> min
                                        },
                                        timeCapMinutes = if (timerMode == TimerMode.FOR_TIME) min else 0,
                                        totalRounds = when (timerMode) {
                                            TimerMode.TABATA -> (min * 60) / (20 + 10)
                                            TimerMode.EMOM -> min
                                            TimerMode.INTERVAL -> (min * 60) / (45 + 15)
                                            else -> 0
                                        },
                                        intervalMinutes = if (timerMode == TimerMode.EMOM) 1 else 0,
                                        blocks = state.blocks
                                    )
                                },
                                label = { Text("${min}m", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ButtonPrimaryContainer,
                                    selectedLabelColor = Green,
                                    containerColor = CardBackground,
                                    labelColor = TimerDim
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            // Workout details during execution
            AnimatedVisibility(
                visible = (state.isRunning || state.isPaused) && state.wodText.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    if (state.wodTitle.isNotBlank()) {
                        Text(
                            text = state.wodTitle,
                            color = TimerWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = state.wodText,
                        color = TimerWhite,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }

            // WOD Editor (when not running)
            AnimatedVisibility(
                visible = !state.isRunning && !state.isPaused && !state.isFinished,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Text("WOD Details", color = TimerDim, fontSize = 12.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    WodEditor(
                        text = state.wodText,
                        onTextChange = { viewModel.updateWodText(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Timer Controls
            TimerControls(
                mode = timerMode,
                isRunning = state.isRunning,
                isPaused = state.isPaused,
                isFinished = state.isFinished,
                manualRounds = state.manualRounds,
                showManualRoundCounter = timerMode == TimerMode.AMRAP || timerMode == TimerMode.FOR_TIME,
                onStart = {
                    viewModel.configure(
                        mode = timerMode,
                        wodText = state.wodText,
                        wodTitle = state.wodTitle,
                        workSeconds = when (timerMode) {
                            TimerMode.TABATA -> 20
                            TimerMode.INTERVAL -> 45
                            else -> 20
                        },
                        restSeconds = when (timerMode) {
                            TimerMode.TABATA -> 10
                            TimerMode.INTERVAL -> 15
                            else -> 10
                        },
                        totalMinutes = when (timerMode) {
                            TimerMode.FOR_TIME -> durationMinutes
                            TimerMode.AMRAP -> durationMinutes
                            TimerMode.EMOM -> durationMinutes
                            else -> durationMinutes
                        },
                        timeCapMinutes = if (timerMode == TimerMode.FOR_TIME) durationMinutes else 0,
                        totalRounds = when (timerMode) {
                            TimerMode.TABATA -> if (durationMinutes > 0) (durationMinutes * 60) / (20 + 10) else 8
                            TimerMode.EMOM -> durationMinutes
                            TimerMode.INTERVAL -> if (durationMinutes > 0) (durationMinutes * 60) / (45 + 15) else 3
                            else -> 0
                        },
                        intervalMinutes = if (timerMode == TimerMode.EMOM) 1 else 0,
                        blocks = state.blocks
                    )
                    viewModel.start()
                },
                onPause = { viewModel.pause() },
                onResume = { viewModel.resume() },
                onReset = { viewModel.reset() },
                onIncrementRound = { viewModel.incrementRound() }
            )

            if (state.isFinished) {
                Text(
                    text = "WORKOUT COMPLETE",
                    color = TimerRed,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
