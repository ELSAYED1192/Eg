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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RadioBand
import com.example.data.model.StationCategory
import com.example.ui.theme.RadioAmberBright
import com.example.ui.theme.RadioAmberGlow
import com.example.ui.theme.RadioBezel
import com.example.ui.theme.RadioChassis
import com.example.ui.theme.RadioChassisCard
import com.example.ui.theme.RadioCyanBright
import com.example.ui.theme.RadioObsidian
import com.example.ui.theme.RadioTextMuted
import com.example.ui.theme.RadioTextPrimary
import com.example.ui.theme.RadioTextSecondary

@Composable
fun AddStationDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onAddStation: (
        name: String,
        frequency: Double,
        band: RadioBand,
        streamUrl: String,
        country: String,
        category: StationCategory,
        programs: List<String>,
        tags: List<String>,
        description: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var name by remember { mutableStateOf("") }
    var frequencyText by remember { mutableStateOf("") }
    var selectedBand by remember { mutableStateOf(RadioBand.SW) }
    var streamUrl by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("مصر") }
    var selectedCategory by remember { mutableStateOf(StationCategory.CULTURE) }
    var programsText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, RadioAmberGlow.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = RadioChassis
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = RadioAmberBright
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إضافة تردد إذاعي جديد",
                            color = RadioTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إلغاء", tint = RadioTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المحطة الإذاعية", color = RadioTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_station_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RadioAmberGlow,
                        unfocusedBorderColor = RadioBezel,
                        focusedTextColor = RadioTextPrimary,
                        unfocusedTextColor = RadioTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Band Selector Pills
                Text("نطاق الموجة:", fontSize = 12.sp, color = RadioTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(RadioBand.SW, RadioBand.MW, RadioBand.LW, RadioBand.FM).forEach { band ->
                        val isSelected = selectedBand == band
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) RadioAmberGlow else RadioChassisCard)
                                .border(1.dp, if (isSelected) RadioAmberBright else RadioBezel, RoundedCornerShape(8.dp))
                                .clickable { selectedBand = band }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = band.code,
                                color = if (isSelected) RadioObsidian else RadioTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Frequency input
                OutlinedTextField(
                    value = frequencyText,
                    onValueChange = { frequencyText = it },
                    label = { Text("التردد (مثال: 9.755 للموجة القصيرة أو 819 للمتوسطة)", color = RadioTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_station_frequency"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RadioAmberGlow,
                        unfocusedBorderColor = RadioBezel,
                        focusedTextColor = RadioTextPrimary,
                        unfocusedTextColor = RadioTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stream URL
                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("رابط البث المباشر (Stream URL)", color = RadioTextSecondary) },
                    placeholder = { Text("http://... أو https://...", color = RadioTextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_station_url"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RadioAmberGlow,
                        unfocusedBorderColor = RadioBezel,
                        focusedTextColor = RadioTextPrimary,
                        unfocusedTextColor = RadioTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Programs
                OutlinedTextField(
                    value = programsText,
                    onValueChange = { programsText = it },
                    label = { Text("أشهر البرامج (افصل بينها بفاصلة)", color = RadioTextSecondary) },
                    placeholder = { Text("برنامج كلمتين وبس، إلى ربات البيوت...", color = RadioTextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RadioAmberGlow,
                        unfocusedBorderColor = RadioBezel,
                        focusedTextColor = RadioTextPrimary,
                        unfocusedTextColor = RadioTextPrimary
                    )
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorText!!, color = RadioAmberBright, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                ElevatedButton(
                    onClick = {
                        val freq = frequencyText.toDoubleOrNull()
                        if (name.isBlank()) {
                            errorText = "يرجى كتابة اسم المحطة"
                        } else if (freq == null) {
                            errorText = "يرجى إدخال تردد رقمي صالح"
                        } else if (streamUrl.isBlank()) {
                            errorText = "يرجى إدخال رابط البث"
                        } else {
                            val programsList = programsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            onAddStation(
                                name,
                                freq,
                                selectedBand,
                                streamUrl,
                                country,
                                selectedCategory,
                                programsList,
                                listOf(selectedBand.code, selectedCategory.titleAr),
                                description
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("submit_add_station"),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = RadioAmberGlow,
                        contentColor = RadioObsidian
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حفظ التردد في الدليل", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
