package com.wodtimer.app.presentation.timer.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wodtimer.app.domain.model.TimerMode
import com.wodtimer.app.presentation.theme.*

@Composable
fun TimerControls(
    mode: TimerMode,
    isRunning: Boolean,
    isPaused: Boolean,
    isFinished: Boolean,
    manualRounds: Int,
    showManualRoundCounter: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onIncrementRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset button
            if (isRunning || isPaused || isFinished) {
                FilledIconButton(
                    onClick = onReset,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = ButtonSecondary,
                        contentColor = TimerWhite
                    )
                ) {
                    Icon(Icons.Filled.Replay, contentDescription = "Reset", modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(20.dp))
            }

            // Start / Pause / Resume
            when {
                !isRunning -> {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .widthIn(min = 160.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("START", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                isPaused -> {
                    Button(
                        onClick = onResume,
                        modifier = Modifier
                            .widthIn(min = 160.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("RESUME", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                else -> {
                    Button(
                        onClick = onPause,
                        modifier = Modifier
                            .widthIn(min = 160.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonDanger,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PAUSE", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            // Manual round counter (AMRAP mode)
            if ((mode == TimerMode.AMRAP || mode == TimerMode.FOR_TIME) && (isRunning || isPaused)) {
                Spacer(Modifier.width(20.dp))
                FilledIconButton(
                    onClick = onIncrementRound,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = ButtonPrimaryContainer,
                        contentColor = Green
                    )
                ) {
                    Icon(Icons.Filled.PlusOne, contentDescription = "Round", modifier = Modifier.size(28.dp))
                }
            }
        }

        // Manual round counter display
        if (showManualRoundCounter && (isRunning || isPaused || isFinished)) {
            Text(
                text = "Rounds: $manualRounds",
                color = TimerWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}
