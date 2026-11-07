package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RadioStation
import com.example.player.PlaybackStatus
import com.example.player.PlayerState
import com.example.ui.theme.RadioAmberBright
import com.example.ui.theme.RadioAmberGlow
import com.example.ui.theme.RadioBezel
import com.example.ui.theme.RadioChassis
import com.example.ui.theme.RadioChassisCard
import com.example.ui.theme.RadioChassisCardElevated
import com.example.ui.theme.RadioCyanBright
import com.example.ui.theme.RadioEmeraldGreen
import com.example.ui.theme.RadioObsidian
import com.example.ui.theme.RadioRubyRed
import com.example.ui.theme.RadioTextMuted
import com.example.ui.theme.RadioTextPrimary
import com.example.ui.theme.RadioTextSecondary

@Composable
fun TuningControls(
    playerState: PlayerState,
    presets: List<RadioStation>,
    onPlayPause: () -> Unit,
    onStepFrequency: (Double) -> Unit,
    onSelectPreset: (RadioStation) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleVolumeBoost: () -> Unit,
    onToggleStatic: () -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSleepMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RadioChassis),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(RadioBezel, RadioObsidian))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Memory Presets Row: P1, P2, P3, P4, P5 (Mechanical radio buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ذاكرة القنوات:",
                    color = RadioTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { presetNum ->
                        val presetStation = presets.find { it.presetNumber == presetNum }
                        val isCurrent = playerState.currentStation?.presetNumber == presetNum

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        isCurrent -> RadioAmberGlow
                                        presetStation != null -> RadioChassisCardElevated
                                        else -> Color(0xFF111722)
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (isCurrent) RadioAmberBright else RadioBezel,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    if (presetStation != null) {
                                        onSelectPreset(presetStation)
                                    }
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("preset_button_$presetNum")
                        ) {
                            Text(
                                text = "P$presetNum",
                                color = when {
                                    isCurrent -> RadioObsidian
                                    presetStation != null -> RadioAmberBright
                                    else -> RadioTextMuted
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Controls: Tuning Step Buttons, Big Play Button, Sleep Timer & Static Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fine-tune backward step
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onStepFrequency(-5.0) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(RadioChassisCard)
                            .border(1.dp, RadioBezel, CircleShape)
                            .testTag("scan_down")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "بحث سريع لأسفل",
                            tint = RadioCyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onStepFrequency(-1.0) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(RadioChassisCard)
                            .border(1.dp, RadioBezel, CircleShape)
                            .testTag("step_down")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "ضبط دقيق لأسفل",
                            tint = RadioAmberBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Big Center Play/Pause Knob
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(RadioAmberBright, RadioAmberGlow, RadioAmberGlow.copy(alpha = 0.85f))
                            )
                        )
                        .border(3.dp, RadioBezel, CircleShape)
                        .clickable { onPlayPause() }
                        .testTag("main_play_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playerState.status == PlaybackStatus.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.status == PlaybackStatus.PLAYING) "إيقاف مؤقت" else "تشغيل",
                        tint = RadioObsidian,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Fine-tune forward step
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onStepFrequency(1.0) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(RadioChassisCard)
                            .border(1.dp, RadioBezel, CircleShape)
                            .testTag("step_up")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "ضبط دقيق لأعلى",
                            tint = RadioAmberBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { onStepFrequency(5.0) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(RadioChassisCard)
                            .border(1.dp, RadioBezel, CircleShape)
                            .testTag("scan_up")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "بحث سريع لأعلى",
                            tint = RadioCyanBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Volume Control Header & 200% Booster Quick Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "مستوى الصوت: ${(playerState.volume * 100).toInt()}%",
                        color = if (playerState.isVolumeBoosted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // 200% Double Volume Boost Toggle Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (playerState.isVolumeBoosted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (playerState.isVolumeBoosted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleVolumeBoost() }
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                        .testTag("volume_boost_200_btn")
                ) {
                    Text(
                        text = if (playerState.isVolumeBoosted) "🔊 تضخيم مضاعف (200%)" else "⚡ رفع الصوت للضعف (200%)",
                        color = if (playerState.isVolumeBoosted) Color.White else MaterialTheme.colorScheme.primary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Volume Control Slider up to 200% (2.0f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                Slider(
                    value = playerState.volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..2.0f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("volume_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = if (playerState.isVolumeBoosted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        activeTrackColor = if (playerState.isVolumeBoosted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (playerState.isVolumeBoosted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Sub-ticks: 0% ... 100% (طبيعي) ... 200% (مضاعف)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "0%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "100% (عادي)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "200% (مضاعف 🔊)", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Extra Toggles: Static Noise (شوشرة الموجات) & Sleep Timer (مؤقت النوم)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Static Noise Simulation Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleStatic() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SurroundSound,
                        contentDescription = null,
                        tint = if (playerState.isStaticNoiseEnabled) RadioCyanBright else RadioTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "صوت شوشرة الأثير",
                        fontSize = 11.sp,
                        color = if (playerState.isStaticNoiseEnabled) RadioCyanBright else RadioTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = playerState.isStaticNoiseEnabled,
                        onCheckedChange = { onToggleStatic() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RadioCyanBright,
                            checkedTrackColor = RadioCyanBright.copy(alpha = 0.3f),
                            uncheckedThumbColor = RadioTextMuted,
                            uncheckedTrackColor = Color(0xFF131A24)
                        ),
                        modifier = Modifier.testTag("static_noise_switch")
                    )
                }

                // Sleep Timer Dropdown
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (playerState.isSleepTimerActive) RadioEmeraldGreen.copy(alpha = 0.2f) else RadioChassisCard)
                            .clickable { showSleepMenu = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("sleep_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = if (playerState.isSleepTimerActive) RadioEmeraldGreen else RadioTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (playerState.isSleepTimerActive) {
                                val mins = playerState.sleepTimerRemainingSeconds / 60
                                val secs = playerState.sleepTimerRemainingSeconds % 60
                                String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
                            } else "مؤقت النوم",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (playerState.isSleepTimerActive) RadioEmeraldGreen else RadioTextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showSleepMenu,
                        onDismissRequest = { showSleepMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("إيقاف المؤقت") },
                            onClick = {
                                onSetSleepTimer(0)
                                showSleepMenu = false
                            }
                        )
                        listOf(15, 30, 45, 60, 90).forEach { mins ->
                            DropdownMenuItem(
                                text = { Text("$mins دقيقة") },
                                onClick = {
                                    onSetSleepTimer(mins)
                                    showSleepMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
