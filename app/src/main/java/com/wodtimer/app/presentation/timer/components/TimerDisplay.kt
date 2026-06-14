package com.wodtimer.app.presentation.timer.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wodtimer.app.domain.model.TimerPhase
import com.wodtimer.app.presentation.theme.*
import com.wodtimer.app.util.TimeFormatter

@Composable
fun TimerDisplay(
    elapsedMillis: Long,
    @Suppress("UNUSED_PARAMETER") totalDurationMillis: Long,
    phase: TimerPhase,
    @Suppress("UNUSED_PARAMETER") modeName: String,
    modifier: Modifier = Modifier
) {
    val displayColor = when (phase) {
        TimerPhase.PREPARE -> TimerYellow
        TimerPhase.WORK -> TimerWhite
        TimerPhase.REST -> TimerYellow
        TimerPhase.COOLDOWN -> Blue
        TimerPhase.FINISHED -> TimerRed
    }

    val formattedTime = remember(elapsedMillis, phase) {
        if (phase == TimerPhase.PREPARE) {
            val seconds = ((elapsedMillis + 999) / 1000).toInt().coerceAtMost(99)
            TimeFormatter.formatCountdown(seconds)
        } else {
            TimeFormatter.formatMillis(elapsedMillis)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = formattedTime,
            color = displayColor,
            fontSize = 96.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 96.sp,
            letterSpacing = 2.sp
        )

        if (phase == TimerPhase.REST) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "REST",
                color = TimerYellow,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        }
    }
}

@Composable
fun RoundIndicator(
    currentRound: Int,
    totalRounds: Int,
    modifier: Modifier = Modifier
) {
    if (totalRounds > 0) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Round",
                color = TimerDim,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$currentRound",
                color = TimerWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " / $totalRounds",
                color = TimerDim,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
