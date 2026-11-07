package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RadioBand
import com.example.data.model.RadioStation
import com.example.data.model.StationCategory
import com.example.player.PlaybackStatus
import com.example.ui.components.StationCard
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
import com.example.ui.viewmodel.RadioUiState

@Composable
fun StationListScreen(
    uiState: RadioUiState,
    onSelectBand: (RadioBand) -> Unit,
    onSelectCategory: (StationCategory) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPlayStation: (RadioStation) -> Unit,
    onToggleFavorite: (RadioStation) -> Unit,
    onAssignPreset: (RadioStation, Int) -> Unit,
    onSyncOnline: () -> Unit,
    onOpenVoiceSearch: () -> Unit,
    onOpenAddStation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RadioObsidian)
    ) {
        // TOP SEARCH BAR & VOICE SEARCH BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("station_search_input"),
                placeholder = {
                    Text(
                        text = "ابحث بالاسم، أو التردد، أو نوع الموسيقى، أو البرنامج...",
                        fontSize = 11.5.sp,
                        color = RadioTextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = RadioTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح",
                                tint = RadioTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RadioAmberGlow,
                    unfocusedBorderColor = RadioBezel,
                    focusedTextColor = RadioTextPrimary,
                    unfocusedTextColor = RadioTextPrimary,
                    focusedContainerColor = RadioChassisCard,
                    unfocusedContainerColor = RadioChassisCard
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Dedicated Voice Search Mic Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RadioAmberGlow)
                    .clickable { onOpenVoiceSearch() }
                    .testTag("voice_search_bar_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "البحث الصوتي",
                    tint = RadioObsidian,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // ONLINE UPDATE & ADD FREQUENCY BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Online Sync Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(RadioChassisCard)
                    .border(1.dp, RadioCyanBright.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .clickable(enabled = !uiState.isOnlineSyncing) { onSyncOnline() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("online_sync_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.isOnlineSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = RadioCyanBright,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = RadioCyanBright,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.isOnlineSyncing) "جاري التحديث..." else "تحديث الترددات عبر الإنترنت",
                    color = RadioCyanBright,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Add Custom Station Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(RadioChassisCard)
                    .border(1.dp, RadioBezel, RoundedCornerShape(10.dp))
                    .clickable { onOpenAddStation() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("add_custom_station_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = RadioAmberBright,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "إضافة تردد",
                    color = RadioAmberBright,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // WAVEBAND FILTER TABS (SW / MW / LW / FM / ALL)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(RadioBand.values()) { band ->
                val isSelected = uiState.selectedBand == band
                val bandColor = when (band) {
                    RadioBand.SW -> RadioRubyRed
                    RadioBand.MW -> RadioAmberBright
                    RadioBand.LW -> RadioEmeraldGreen
                    RadioBand.FM -> RadioCyanBright
                    RadioBand.ALL -> RadioAmberGlow
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) bandColor else RadioChassisCard)
                        .border(
                            1.dp,
                            if (isSelected) bandColor else RadioBezel,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectBand(band) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("band_filter_${band.code}")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (band == RadioBand.ALL) "جميع الموجات" else "${band.titleAr} (${band.code})",
                            color = if (isSelected) RadioObsidian else RadioTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (band != RadioBand.ALL) {
                            Text(
                                text = band.rangeDisplay,
                                color = if (isSelected) RadioObsidian.copy(alpha = 0.8f) else RadioTextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // CATEGORY / GENRE CHIPS (قرآن، أخبار، رياضة، كلاسيك، طرب...)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(StationCategory.values()) { category ->
                val isSelected = uiState.selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) RadioAmberGlow.copy(alpha = 0.25f) else Color(0xFF131A24))
                        .border(
                            1.dp,
                            if (isSelected) RadioAmberGlow else RadioBezel,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectCategory(category) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("category_filter_${category.name}")
                ) {
                    Text(
                        text = category.titleAr,
                        color = if (isSelected) RadioAmberBright else RadioTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // STATIONS COUNT & STATUS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "المحطات المتاحة (${uiState.stations.size})",
                color = RadioTextMuted,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // MAIN STATIONS LIST
        if (uiState.stations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = RadioTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "لا توجد إذاعات متطابقة مع تصنيفك أو بحثك الحالي.",
                        color = RadioTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(RadioAmberGlow)
                            .clickable {
                                onSelectBand(RadioBand.ALL)
                                onSelectCategory(StationCategory.ALL)
                                onSearchQueryChange("")
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "إعادة ضبط كل الفلاتر",
                            color = RadioObsidian,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.stations, key = { it.id }) { station ->
                    val isCurrentlyPlaying = uiState.playerState.currentStation?.id == station.id &&
                            (uiState.playerState.status == PlaybackStatus.PLAYING || uiState.playerState.status == PlaybackStatus.BUFFERING)

                    StationCard(
                        station = station,
                        isPlaying = isCurrentlyPlaying,
                        onPlayToggle = { onPlayStation(station) },
                        onFavoriteToggle = { onToggleFavorite(station) },
                        onAssignPreset = { presetNum -> onAssignPreset(station, presetNum) }
                    )
                }
            }
        }
    }
}
