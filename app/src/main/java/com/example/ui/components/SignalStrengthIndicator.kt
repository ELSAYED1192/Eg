package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Returns color according to signal strength percentage:
 * - 75%..100%: Emerald Green (Excellent)
 * - 50%..74%: Amber Bright (Good/Moderate)
 * - 25%..49%: Amber Dark (Fair)
 * - 0%..24%: Ruby Red (Weak)
 */
fun getSignalColor(percent: Int): Color = when {
    percent >= 75 -> RadioEmeraldGreen
    percent >= 50 -> RadioAmberBright
    percent >= 25 -> RadioAmberGlow
    else -> RadioRubyRed
}

/**
 * Descriptive label in Arabic for signal strength
 */
fun getSignalLabel(percent: Int): String = when {
    percent >= 85 -> "إشارة ممتازة • S9+20dB"
    percent >= 70 -> "إشارة جيدة • S7-S9"
    percent >= 50 -> "إشارة متوسطة • S5"
    percent >= 25 -> "إشارة ضعيفة • S3"
    else -> "تشويش / جاري الالتقاط"
}

/**
 * Calculates estimated dBm based on signal percentage
 */
fun getEstimatedDbm(percent: Int): Int {
    // Maps 0..100% to -105 dBm (poor) to -50 dBm (strong broadcast)
    return (-105 + (percent * 0.55f)).toInt()
}

/**
 * Standalone Broadcast Tower Icon with tiered concentric signal radiation waves
 */
@Composable
fun BroadcastTowerSignalIcon(
    strengthPercent: Int,
    isReceiving: Boolean = true,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp
) {
    val activeColor = getSignalColor(strengthPercent)
    val infiniteTransition = rememberInfiniteTransition(label = "signalWave")
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveAlpha"
    )

    Canvas(modifier = modifier.size(iconSize).testTag("broadcast_tower_signal_icon")) {
        val w = size.width
        val h = size.height

        // Tower center mast
        val mastPath = Path().apply {
            moveTo(w * 0.5f, h * 0.25f)
            lineTo(w * 0.25f, h * 0.95f)
            moveTo(w * 0.5f, h * 0.25f)
            lineTo(w * 0.75f, h * 0.95f)
            // Crossbars
            moveTo(w * 0.35f, h * 0.65f)
            lineTo(w * 0.65f, h * 0.65f)
            moveTo(w * 0.42f, h * 0.45f)
            lineTo(w * 0.58f, h * 0.45f)
        }
        drawPath(
            path = mastPath,
            color = if (isReceiving) activeColor else RadioTextMuted,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        )

        // Transmitter top emitter bead
        drawCircle(
            color = if (isReceiving) activeColor else RadioTextMuted,
            radius = 2.2.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.22f)
        )

        // Tiered Radio Waves (Arc 1 and Arc 2 radiating from mast head)
        val numArcs = when {
            strengthPercent >= 75 -> 2
            strengthPercent >= 40 -> 1
            else -> 0
        }

        if (isReceiving && numArcs >= 1) {
            val alpha1 = if (strengthPercent >= 75) waveAlpha else 0.6f
            // Left Arc 1
            drawArc(
                color = activeColor.copy(alpha = alpha1),
                startAngle = 135f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.2f, h * 0.02f),
                size = Size(w * 0.6f, h * 0.4f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        if (isReceiving && numArcs >= 2) {
            // Left Arc 2 (outer wave)
            drawArc(
                color = activeColor.copy(alpha = waveAlpha * 0.75f),
                startAngle = 125f,
                sweepAngle = 110f,
                useCenter = false,
                topLeft = Offset(w * 0.05f, -h * 0.12f),
                size = Size(w * 0.9f, h * 0.68f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * 5-Bar Cellular / Antenna Tower Signal Meter (أيقونات الأبراج)
 */
@Composable
fun CellularTowerBars(
    strengthPercent: Int,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    barSpacing: Dp = 2.5.dp
) {
    val activeColor = getSignalColor(strengthPercent)
    val activeTiers = when {
        strengthPercent >= 85 -> 5
        strengthPercent >= 65 -> 4
        strengthPercent >= 45 -> 3
        strengthPercent >= 25 -> 2
        strengthPercent > 0 -> 1
        else -> 0
    }

    Row(
        modifier = modifier.testTag("cellular_tower_bars"),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(barSpacing)
    ) {
        for (i in 1..barCount) {
            val isBarActive = i <= activeTiers
            val barHeightFraction = i.toFloat() / barCount.toFloat()

            Box(
                modifier = Modifier
                    .width(4.5.dp)
                    .height((6 + (i * 3.5)).dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        if (isBarActive) {
                            when {
                                i >= 4 -> RadioEmeraldGreen
                                i == 3 -> RadioAmberBright
                                else -> RadioAmberGlow
                            }
                        } else Color(0xFF242E3D)
                    )
            )
        }
    }
}

/**
 * Graduated continuous or segmented gradient bar (شريط متدرج لعرض قوة الإشارة)
 */
@Composable
fun GraduatedSignalBar(
    strengthPercent: Int,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp
) {
    val animatedPercent by animateFloatAsState(
        targetValue = (strengthPercent.coerceIn(0, 100) / 100f),
        animationSpec = tween(durationMillis = 300),
        label = "signalBar"
    )

    Column(modifier = modifier) {
        // Gradient Bar Track with Glowing Fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(Color(0xFF0F1520))
                .border(1.dp, RadioBezel, RoundedCornerShape(height / 2))
        ) {
            // Filled graduated portion
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercent)
                    .height(height)
                    .clip(RoundedCornerShape(height / 2))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                RadioRubyRed,
                                RadioAmberGlow,
                                RadioAmberBright,
                                RadioEmeraldGreen
                            )
                        )
                    )
            )

            // Segment Notch Tick Lines overlay
            Canvas(modifier = Modifier.matchParentSize()) {
                val segments = 10
                val step = size.width / segments
                for (s in 1 until segments) {
                    val x = s * step
                    drawLine(
                        color = Color(0x33000000),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }
    }
}

/**
 * Complete, prominent Signal Reception Panel for the Radio Tuner Screen
 */
@Composable
fun SignalReceptionPanel(
    strengthPercent: Int,
    isLive: Boolean = true,
    frequencyDisplay: String? = null,
    modifier: Modifier = Modifier
) {
    val signalColor = getSignalColor(strengthPercent)
    val signalLabel = getSignalLabel(strengthPercent)
    val estimatedDbm = getEstimatedDbm(strengthPercent)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0B1017))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        RadioBezel,
                        signalColor.copy(alpha = 0.6f),
                        RadioBezel
                    )
                ),
                RoundedCornerShape(12.dp)
            )
            .testTag("signal_reception_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Header Row: Tower Icon, Signal Label, Decibels (dBm) & Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tower & Label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BroadcastTowerSignalIcon(
                        strengthPercent = strengthPercent,
                        isReceiving = isLive,
                        iconSize = 22.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مؤشر إشارة الأثير",
                                color = RadioTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Status pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(signalColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = signalLabel,
                                    color = signalColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Numerical Metrics: Percentage and RSSI dBm
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$estimatedDbm dBm",
                        color = RadioTextMuted,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$strengthPercent%",
                        color = signalColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    CellularTowerBars(
                        strengthPercent = strengthPercent,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Graduated Signal Bar (الشريط المتدرج)
            GraduatedSignalBar(
                strengthPercent = strengthPercent,
                modifier = Modifier.fillMaxWidth(),
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-scale Calibration Legend: S1 ... S5 ... S9 ... +20dB
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("S1 (ضعيفة)", "S3", "S5 (متوسطة)", "S7", "S9", "+20dB (ممتازة)").forEach { tick ->
                    Text(
                        text = tick,
                        color = RadioTextMuted,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Compact Pill Badge for list items and cards (أيقونة الأبراج مع النسبة)
 */
@Composable
fun CompactSignalBadge(
    strengthPercent: Int,
    modifier: Modifier = Modifier
) {
    val color = getSignalColor(strengthPercent)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CellularTowerBars(
            strengthPercent = strengthPercent,
            barCount = 4,
            barSpacing = 1.5.dp
        )
        Text(
            text = "$strengthPercent%",
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
