package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RadioBand
import com.example.data.model.RadioStation
import com.example.ui.components.RetroFrequencyDisplay
import com.example.ui.components.TuningControls
import com.example.ui.theme.RadioAmberBright
import com.example.ui.theme.RadioAmberGlow
import com.example.ui.theme.RadioBezel
import com.example.ui.theme.RadioChassisCard
import com.example.ui.theme.RadioChassisCardElevated
import com.example.ui.theme.RadioCyanBright
import com.example.ui.theme.RadioCyanGlow
import com.example.ui.theme.RadioEmeraldGreen
import com.example.ui.theme.RadioObsidian
import com.example.ui.theme.RadioRubyRed
import com.example.ui.theme.RadioTextMuted
import com.example.ui.theme.RadioTextPrimary
import com.example.ui.theme.RadioTextSecondary
import com.example.ui.viewmodel.RadioUiState

@Composable
fun TunerScreen(
    uiState: RadioUiState,
    onFrequencyDrag: (Double) -> Unit,
    onStepFrequency: (Double) -> Unit,
    onPlayPause: () -> Unit,
    onSelectPreset: (RadioStation) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleVolumeBoost: () -> Unit,
    onToggleStatic: () -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onOpenVoiceSearch: () -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // QUICK VOICE SEARCH CTA BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1E2838), Color(0xFF161F2C))
                    )
                )
                .border(1.dp, RadioAmberGlow.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .clickable { onOpenVoiceSearch() }
                .padding(12.dp)
                .testTag("tuner_voice_cta")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RadioAmberGlow.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = RadioAmberBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "البحث الصوتي السريع",
                            color = RadioTextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "انطق: اسم المحطة، أو نوع الموسيقى، أو اسم البرنامج",
                            color = RadioTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RadioAmberGlow)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "تحدث الآن",
                        color = RadioObsidian,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // RETRO GLOWING FREQUENCY SCREEN
        RetroFrequencyDisplay(
            currentFrequency = uiState.currentTunedFrequency,
            selectedBand = uiState.selectedBand,
            currentStation = uiState.playerState.currentStation,
            playerState = uiState.playerState,
            onFrequencyDrag = onFrequencyDrag,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // HARDWARE-STYLE TUNING CONTROLS (P1-P5, Step Knobs, Volume, Sleep Timer)
        TuningControls(
            playerState = uiState.playerState,
            presets = uiState.presets,
            onPlayPause = onPlayPause,
            onStepFrequency = onStepFrequency,
            onSelectPreset = onSelectPreset,
            onVolumeChange = onVolumeChange,
            onToggleVolumeBoost = onToggleVolumeBoost,
            onToggleStatic = onToggleStatic,
            onSetSleepTimer = onSetSleepTimer,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // NOW PLAYING STATION INFO CARD
        val currentStation = uiState.playerState.currentStation
        if (currentStation != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RadioChassisCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(RadioBezel)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = currentStation.countryFlag, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currentStation.name,
                                    color = RadioTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${currentStation.band.titleAr} • ${currentStation.frequencyDisplay}",
                                    color = RadioCyanBright,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = { onToggleFavorite(currentStation) }
                        ) {
                            Icon(
                                imageVector = if (currentStation.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "حفظ في المفضلة",
                                tint = if (currentStation.isFavorite) RadioRubyRed else RadioTextMuted
                            )
                        }
                    }

                    if (currentStation.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentStation.description,
                            color = RadioTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    if (currentStation.popularPrograms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "أشهر برامج هذه المحطة الإذاعية:",
                            color = RadioTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            currentStation.popularPrograms.forEach { prog ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = RadioAmberBright,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = prog,
                                        color = RadioTextPrimary,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
