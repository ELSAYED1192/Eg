package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.ui.components.ThemeSettingsDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.PlaybackStatus
import com.example.ui.components.AddStationDialog
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.theme.RadioAmberBright
import com.example.ui.theme.RadioAmberGlow
import com.example.ui.theme.RadioBezel
import com.example.ui.theme.RadioChassis
import com.example.ui.theme.RadioChassisCard
import com.example.ui.theme.RadioChassisCardElevated
import com.example.ui.theme.RadioCyanBright
import com.example.ui.theme.RadioObsidian
import com.example.ui.theme.RadioRubyRed
import com.example.ui.theme.RadioTextMuted
import com.example.ui.theme.RadioTextPrimary
import com.example.ui.theme.RadioTextSecondary
import com.example.ui.viewmodel.RadioUiState
import com.example.ui.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioMainScreen(
    viewModel: RadioViewModel,
    uiState: RadioUiState,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.syncFeedbackMessage) {
        val msg = uiState.syncFeedbackMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSyncFeedback()
        }
    }

    val isCustomWallpaperActive = uiState.themeConfig.customWallpaperUri != null
    val isPresetWallpaperActive = uiState.themeConfig.presetWallpaperIndex > 0

    Box(modifier = modifier.fillMaxSize()) {
        // Dynamic Wallpaper Backdrop from Phone Gallery or Atmospheric Preset
        if (isCustomWallpaperActive) {
            AsyncImage(
                model = uiState.themeConfig.customWallpaperUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(uiState.themeConfig.wallpaperBlurDp.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = uiState.themeConfig.wallpaperDimAlpha))
            )
        } else if (isPresetWallpaperActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (uiState.themeConfig.presetWallpaperIndex) {
                            1 -> androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0B132B)))
                            2 -> androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF2E0E1F), Color(0xFF180D1D), Color(0xFF100713)))
                            3 -> androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF060D17), Color(0xFF0F2642), Color(0xFF08192E)))
                            else -> androidx.compose.ui.graphics.Brush.verticalGradient(listOf(RadioObsidian, Color(0xFF0A0F18)))
                        }
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = uiState.themeConfig.wallpaperDimAlpha.coerceAtLeast(0.3f)))
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = if (isCustomWallpaperActive || isPresetWallpaperActive) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = RadioAmberBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "راديو الأثير",
                                color = RadioTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "الموجات القصيرة • المتوسطة • الطويلة • FM",
                            color = RadioAmberGlow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.openAddStationDialog() },
                        modifier = Modifier.testTag("topbar_add_station")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة تردد جديد",
                            tint = RadioAmberBright
                        )
                    }
                },
                actions = {
                    // Online Sync Button
                    IconButton(
                        onClick = { viewModel.syncFrequenciesOnline() },
                        modifier = Modifier.testTag("topbar_online_sync")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "تحديث الترددات عبر الإنترنت",
                            tint = RadioCyanBright
                        )
                    }

                    // Theme, Colors & Wallpapers Customization Button
                    IconButton(
                        onClick = { viewModel.openThemeDialog() },
                        modifier = Modifier.testTag("topbar_theme_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "المظهر والألوان والخلفيات",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Voice Search Button
                    IconButton(
                        onClick = { viewModel.openVoiceSearch() },
                        modifier = Modifier.testTag("topbar_voice_search")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RadioAmberGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "البحث الصوتي المتقدم",
                                tint = RadioObsidian,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isCustomWallpaperActive || isPresetWallpaperActive) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Persistent Mini Player (visible on tabs 1 & 2 when station is loaded)
                val currentStation = uiState.playerState.currentStation
                if (uiState.activeTab != 0 && currentStation != null) {
                    MiniPlayerBar(
                        station = currentStation,
                        isPlaying = uiState.playerState.status == PlaybackStatus.PLAYING,
                        isBuffering = uiState.playerState.status == PlaybackStatus.BUFFERING,
                        signalPercent = uiState.playerState.signalStrengthPercent,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onClick = { viewModel.setActiveTab(0) }
                    )
                }

                // Material 3 Navigation Bar
                NavigationBar(
                    containerColor = if (isCustomWallpaperActive || isPresetWallpaperActive) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        selected = uiState.activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = {
                            Icon(imageVector = Icons.Default.Radio, contentDescription = "جهاز الراديو")
                        },
                        label = { Text("جهاز الراديو", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RadioObsidian,
                            selectedTextColor = RadioAmberBright,
                            indicatorColor = RadioAmberGlow,
                            unselectedIconColor = RadioTextSecondary,
                            unselectedTextColor = RadioTextMuted
                        ),
                        modifier = Modifier.testTag("nav_tuner_tab")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = {
                            Icon(imageVector = Icons.Default.ViewList, contentDescription = "دليل المحطات")
                        },
                        label = { Text("دليل المحطات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RadioObsidian,
                            selectedTextColor = RadioAmberBright,
                            indicatorColor = RadioAmberGlow,
                            unselectedIconColor = RadioTextSecondary,
                            unselectedTextColor = RadioTextMuted
                        ),
                        modifier = Modifier.testTag("nav_stations_tab")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = {
                            BadgedBox(badge = {
                                if (uiState.favorites.isNotEmpty()) {
                                    Badge(containerColor = RadioRubyRed) {
                                        Text("${uiState.favorites.size}", color = Color.White)
                                    }
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = "المفضلة")
                            }
                        },
                        label = { Text("المفضلة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RadioObsidian,
                            selectedTextColor = RadioAmberBright,
                            indicatorColor = RadioAmberGlow,
                            unselectedIconColor = RadioTextSecondary,
                            unselectedTextColor = RadioTextMuted
                        ),
                        modifier = Modifier.testTag("nav_favorites_tab")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                0 -> TunerScreen(
                    uiState = uiState,
                    onFrequencyDrag = { freq -> viewModel.setTunedFrequency(freq) },
                    onStepFrequency = { step -> viewModel.stepFrequency(step) },
                    onPlayPause = { viewModel.togglePlayPause() },
                    onSelectPreset = { station -> viewModel.playStation(station) },
                    onVolumeChange = { vol -> viewModel.setVolume(vol) },
                    onToggleVolumeBoost = { viewModel.toggleVolumeBoost() },
                    onToggleStatic = { viewModel.toggleStaticNoise() },
                    onSetSleepTimer = { mins -> viewModel.setSleepTimer(mins) },
                    onOpenVoiceSearch = { viewModel.openVoiceSearch() },
                    onToggleFavorite = { station -> viewModel.toggleFavorite(station) }
                )

                1 -> StationListScreen(
                    uiState = uiState,
                    onSelectBand = { band -> viewModel.selectBand(band) },
                    onSelectCategory = { cat -> viewModel.selectCategory(cat) },
                    onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                    onPlayStation = { station -> viewModel.playStation(station) },
                    onToggleFavorite = { station -> viewModel.toggleFavorite(station) },
                    onAssignPreset = { station, pNum -> viewModel.assignPreset(station, pNum) },
                    onSyncOnline = { viewModel.syncFrequenciesOnline() },
                    onOpenVoiceSearch = { viewModel.openVoiceSearch() },
                    onOpenAddStation = { viewModel.openAddStationDialog() }
                )

                2 -> FavoritesScreen(
                    uiState = uiState,
                    onPlayStation = { station -> viewModel.playStation(station) },
                    onToggleFavorite = { station -> viewModel.toggleFavorite(station) },
                    onAssignPreset = { station, pNum -> viewModel.assignPreset(station, pNum) },
                    onNavigateToStations = { viewModel.setActiveTab(1) }
                )
            }
        }
    }

    // Advanced Voice Search Dialog
    VoiceSearchDialog(
        isOpen = uiState.isVoiceSearchDialogOpen,
        searchResult = uiState.voiceSearchResult,
        onDismiss = {
            viewModel.closeVoiceSearch()
            viewModel.clearVoiceSearchResult()
        },
        onVoiceQuery = { spokenQuery ->
            viewModel.processVoiceQuery(spokenQuery)
        },
        onSelectStation = { station ->
            viewModel.playStation(station)
            viewModel.closeVoiceSearch()
            viewModel.clearVoiceSearchResult()
        }
    )

    // Add Custom Station Dialog
    AddStationDialog(
        isOpen = uiState.showAddStationDialog,
        onDismiss = { viewModel.closeAddStationDialog() },
        onAddStation = { name, freq, band, url, country, cat, progs, tags, desc ->
            viewModel.addCustomStation(name, freq, band, url, country, cat, progs, tags, desc)
        }
    )

    // Theme, Colors & Custom Phone Wallpapers Customization Dialog
    ThemeSettingsDialog(
        isOpen = uiState.showThemeDialog,
        themeConfig = uiState.themeConfig,
        onDismiss = { viewModel.closeThemeDialog() },
        onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
        onSetPalette = { palette -> viewModel.setColorPalette(palette) },
        onSelectCustomWallpaper = { uri -> viewModel.setCustomWallpaper(uri) },
        onSelectPresetWallpaper = { index -> viewModel.setPresetWallpaper(index) },
        onSetWallpaperBlur = { blur -> viewModel.setWallpaperBlur(blur) },
        onSetWallpaperDim = { dim -> viewModel.setWallpaperDim(dim) }
    )
    } // Closes outer wallpaper Box
}

@Composable
fun MiniPlayerBar(
    station: com.example.data.model.RadioStation,
    isPlaying: Boolean,
    isBuffering: Boolean,
    signalPercent: Int,
    onPlayPause: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .background(RadioChassisCardElevated)
            .border(1.dp, RadioBezel, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(RadioAmberGlow.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = station.countryFlag, fontSize = 16.sp)
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
                        text = "${station.band.code} • ${station.frequencyDisplay} ${if (isBuffering) "• جاري الاتصال..." else ""}",
                        color = RadioCyanBright,
                        fontSize = 11.sp
                    )
                    if (isPlaying) {
                        Spacer(modifier = Modifier.width(6.dp))
                        com.example.ui.components.CompactSignalBadge(
                            strengthPercent = signalPercent
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(RadioAmberGlow)
                .clickable { onPlayPause() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                tint = RadioObsidian,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
