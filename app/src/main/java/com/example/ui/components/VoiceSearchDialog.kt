package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.RadioStation
import com.example.ui.theme.RadioAmberBright
import com.example.ui.theme.RadioAmberGlow
import com.example.ui.theme.RadioBezel
import com.example.ui.theme.RadioChassis
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
import com.example.util.VoiceIntentType
import com.example.util.VoiceSearchResult
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceSearchDialog(
    isOpen: Boolean,
    searchResult: VoiceSearchResult?,
    onDismiss: () -> Unit,
    onVoiceQuery: (String) -> Unit,
    onSelectStation: (RadioStation) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf("") }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                onVoiceQuery(spoken)
            }
        }
    }

    fun triggerSpeechRecognition() {
        try {
            isListening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث باسم المحطة، أو نوع الموسيقى، أو اسم البرنامج...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            isListening = false
            Toast.makeText(context, "خاصية التعرف على الصوت غير متاحة في هذا الجهاز، يمكنك الكتابة في المربع.", Toast.LENGTH_LONG).show()
        }
    }

    // Auto-launch voice prompt on open if no search result yet
    LaunchedEffect(isOpen) {
        if (searchResult == null) {
            triggerSpeechRecognition()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, RadioAmberGlow.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            color = RadioChassis
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RadioAmberGlow.copy(alpha = 0.2f)),
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
                                text = "البحث الصوتي الذكي",
                                color = RadioTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "محطة • نوع موسيقى • اسم برنامج • تردد",
                                color = RadioTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_voice_search")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = RadioTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsing Mic Circle Button
                Box(
                    modifier = Modifier
                        .size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(RadioAmberGlow.copy(alpha = 0.25f))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(RadioAmberBright, RadioAmberGlow, RadioAmberGlow.copy(alpha = 0.8f))
                                )
                            )
                            .clickable { triggerSpeechRecognition() }
                            .testTag("voice_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "تحدث الآن",
                            tint = RadioObsidian,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isListening) "🎙️ جارٍ الاستماع... تحدث بوضوح" else "انقر على الميكروفون للتحدث مرة أخرى",
                    color = if (isListening) RadioAmberBright else RadioTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Voice Prompts Chips
                Text(
                    text = "أمثلة يمكنك نطقها الآن:",
                    color = RadioTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "موسيقى كلاسيكية",
                        "إذاعة القرآن الكريم",
                        "أخبار رياضية",
                        "برنامج كلمتين وبس",
                        "طرب وأغاني زمان",
                        "صوت العرب",
                        "موجة قصيرة"
                    ).forEach { sample ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(RadioChassisCardElevated)
                                .border(1.dp, RadioBezel, RoundedCornerShape(12.dp))
                                .clickable {
                                    onVoiceQuery(sample)
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🗣️ $sample",
                                color = RadioCyanBright,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // If user wants to type or edit spoken text
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualText,
                        onValueChange = { manualText = it },
                        placeholder = { Text("أو اكتب هنا للبحث...", fontSize = 12.sp, color = RadioTextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voice_manual_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadioAmberGlow,
                            unfocusedBorderColor = RadioBezel,
                            focusedTextColor = RadioTextPrimary,
                            unfocusedTextColor = RadioTextPrimary,
                            focusedContainerColor = RadioChassisCard,
                            unfocusedContainerColor = RadioChassisCard
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = {
                            if (manualText.isNotBlank()) {
                                onVoiceQuery(manualText)
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = RadioAmberGlow,
                            contentColor = RadioObsidian
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("voice_submit_button")
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "بحث")
                    }
                }

                // SEARCH RESULT SECTION
                if (searchResult != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = RadioBezel, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Detected Spoken Query & Intent Badge
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RadioChassisCard)
                            .border(1.dp, RadioAmberGlow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نطقت: \"${searchResult.spokenQuery}\"",
                                color = RadioTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val intentBadgeColor = when (searchResult.intentType) {
                                VoiceIntentType.GENRE -> RadioEmeraldGreen
                                VoiceIntentType.PROGRAM_NAME -> RadioCyanBright
                                VoiceIntentType.STATION_NAME -> RadioAmberBright
                                VoiceIntentType.FREQUENCY -> RadioRubyRed
                                else -> RadioTextSecondary
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(intentBadgeColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = searchResult.intentType.titleAr,
                                    color = intentBadgeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!searchResult.matchedDetail.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = searchResult.matchedDetail,
                                color = RadioTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Matched Stations List
                    Text(
                        text = "المحطات المتطابقة (${searchResult.matchedStations.size}):",
                        color = RadioTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (searchResult.matchedStations.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لم يتم العثور على محطات مطابقة تماماً. جرّب نطق نوع الموسيقى أو اسم المحطة بوضوح.",
                                color = RadioTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResult.matchedStations) { station ->
                                MatchedVoiceStationItem(
                                    station = station,
                                    onPlay = {
                                        onSelectStation(station)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchedVoiceStationItem(
    station: RadioStation,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RadioChassisCardElevated)
            .border(1.dp, RadioBezel, RoundedCornerShape(12.dp))
            .clickable { onPlay() }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(RadioAmberGlow.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = station.countryFlag,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = station.name,
                    color = RadioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${station.band.code} • ${station.frequencyDisplay}",
                        color = RadioCyanBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${station.category.titleAr}",
                        color = RadioTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        ElevatedButton(
            onClick = onPlay,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = RadioAmberGlow,
                contentColor = RadioObsidian
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "تشغيل",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("تشغيل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
