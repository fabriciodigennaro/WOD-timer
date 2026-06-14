package com.wodtimer.app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wodtimer.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TimerWhite)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text("Sound & Vibration", color = Green, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
            SettingsSwitch(
                title = "Sound Effects",
                subtitle = "Beeps and alerts during workout",
                icon = Icons.Outlined.VolumeUp,
                checked = settings.soundEnabled,
                onToggle = { viewModel.toggleSound() }
            )
            SettingsSwitch(
                title = "Vibration",
                subtitle = "Haptic feedback at phase changes",
                icon = Icons.Outlined.Vibration,
                checked = settings.vibrationEnabled,
                onToggle = { viewModel.toggleVibration() }
            )
            SettingsSwitch(
                title = "Voice (TTS)",
                subtitle = "\"3, 2, 1, GO\", \"Rest\", \"Last round\"",
                icon = Icons.Outlined.RecordVoiceOver,
                checked = settings.ttsEnabled,
                onToggle = { viewModel.toggleTts() }
            )

            Spacer(Modifier.height(16.dp))

            Text("Timer", color = Green, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
            SettingsSlider(
                title = "Prepare Countdown",
                subtitle = "${settings.prepareCountdown} seconds",
                icon = Icons.Outlined.Timer,
                value = settings.prepareCountdown.toFloat(),
                valueRange = 0f..15f,
                steps = 14,
                onValueChange = { viewModel.setPrepareCountdown(it.toInt()) }
            )
            SettingsSlider(
                title = "Sound Volume",
                subtitle = "${(settings.soundVolume * 100).toInt()}%",
                icon = Icons.Outlined.VolumeDown,
                value = settings.soundVolume,
                valueRange = 0f..1f,
                steps = 9,
                onValueChange = { viewModel.setVolume(it) }
            )
            SettingsSwitch(
                title = "Beep at Each Minute",
                subtitle = "For Time mode",
                icon = Icons.Outlined.Notifications,
                checked = settings.beepAtEachMinute,
                onToggle = { viewModel.toggleBeepEachMinute() }
            )
            SettingsSwitch(
                title = "Last Minute Warning",
                subtitle = "Voice alert at 60 seconds remaining",
                icon = Icons.Outlined.Warning,
                checked = settings.lastBeepWarning,
                onToggle = { viewModel.toggleLastBeepWarning() }
            )

            Spacer(Modifier.height(16.dp))

            Text("Display", color = Green, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
            SettingsSwitch(
                title = "Keep Screen Awake",
                subtitle = "Screen stays on during workout",
                icon = Icons.Outlined.BrightnessHigh,
                checked = settings.keepScreenAwake,
                onToggle = { viewModel.toggleKeepAwake() }
            )

            Spacer(Modifier.height(16.dp))

            Text("About", color = Green, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("WODTimer", color = TimerWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Version 1.2.0", color = TimerDim, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "A premium CrossFit timer for functional fitness training. " +
                                "Supports For Time, EMOM, Tabata, AMRAP, Interval, and Custom WOD modes.",
                        color = TimerDim,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TimerDim,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TimerWhite, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(text = subtitle, color = TimerDim, fontSize = 12.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Green,
                    checkedTrackColor = ButtonPrimaryContainer,
                    uncheckedThumbColor = TimerDim,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun SettingsSlider(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TimerDim,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = title, color = TimerWhite, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    Text(text = subtitle, color = TimerDim, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = Green,
                    activeTrackColor = Green,
                    inactiveTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}
