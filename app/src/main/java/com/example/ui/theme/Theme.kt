package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun getCustomColorScheme(
    isDark: Boolean,
    paletteType: ColorPaletteType
): ColorScheme {
    return if (isDark) {
        when (paletteType) {
            ColorPaletteType.NEON_CYBER -> darkColorScheme(
                primary = NeonCyan,
                onPrimary = RadioObsidian,
                primaryContainer = RadioChassisCardElevated,
                onPrimaryContainer = NeonCyan,
                secondary = NeonPink,
                onSecondary = RadioObsidian,
                secondaryContainer = RadioChassisCard,
                onSecondaryContainer = NeonPink,
                tertiary = NeonPurple,
                background = RadioObsidian,
                onBackground = RadioTextPrimary,
                surface = RadioChassis,
                onSurface = RadioTextPrimary,
                surfaceVariant = RadioChassisCard,
                onSurfaceVariant = RadioTextSecondary,
                outline = RadioBezel,
                error = RadioRubyRed
            )
            ColorPaletteType.ROYAL_AMBER -> darkColorScheme(
                primary = RadioAmberGlow,
                onPrimary = RadioObsidian,
                primaryContainer = RadioChassisCardElevated,
                onPrimaryContainer = RadioAmberBright,
                secondary = RadioCyanGlow,
                onSecondary = RadioObsidian,
                secondaryContainer = RadioChassisCard,
                onSecondaryContainer = RadioCyanBright,
                tertiary = SunsetOrange,
                background = RadioObsidian,
                onBackground = RadioTextPrimary,
                surface = RadioChassis,
                onSurface = RadioTextPrimary,
                surfaceVariant = RadioChassisCard,
                onSurfaceVariant = RadioTextSecondary,
                outline = RadioBorder,
                error = RadioRubyRed
            )
            ColorPaletteType.ELECTRIC_EMERALD -> darkColorScheme(
                primary = RadioEmeraldGreen,
                onPrimary = RadioObsidian,
                primaryContainer = Color(0xFF0D251A),
                onPrimaryContainer = RadioMintGlow,
                secondary = RadioCyanGlow,
                onSecondary = RadioObsidian,
                secondaryContainer = Color(0xFF091C14),
                onSecondaryContainer = RadioMintGlow,
                tertiary = NeonYellow,
                background = Color(0xFF07110D),
                onBackground = RadioTextPrimary,
                surface = Color(0xFF0C1D16),
                onSurface = RadioTextPrimary,
                surfaceVariant = Color(0xFF13281E),
                onSurfaceVariant = Color(0xFFA7D4BE),
                outline = Color(0xFF1D3E2F),
                error = RadioRubyRed
            )
            ColorPaletteType.COSMIC_SUNSET -> darkColorScheme(
                primary = SunsetCoral,
                onPrimary = Color.White,
                primaryContainer = Color(0xFF2E0E1F),
                onPrimaryContainer = SunsetCoral,
                secondary = SunsetOrange,
                onSecondary = RadioObsidian,
                secondaryContainer = Color(0xFF220A16),
                onSecondaryContainer = SunsetOrange,
                tertiary = SunsetViolet,
                background = Color(0xFF100713),
                onBackground = RadioTextPrimary,
                surface = Color(0xFF180D1D),
                onSurface = RadioTextPrimary,
                surfaceVariant = Color(0xFF24132B),
                onSurfaceVariant = Color(0xFFD4B0DD),
                outline = Color(0xFF42224F),
                error = RadioRubyRed
            )
            ColorPaletteType.DEEP_SAPPHIRE -> darkColorScheme(
                primary = Color(0xFF00B4D8),
                onPrimary = RadioObsidian,
                primaryContainer = Color(0xFF0B2138),
                onPrimaryContainer = Color(0xFF48CAE4),
                secondary = Color(0xFF9D4EDD),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFF081829),
                onSecondaryContainer = Color(0xFFC77DFF),
                tertiary = Color(0xFF0077B6),
                background = Color(0xFF060D17),
                onBackground = RadioTextPrimary,
                surface = Color(0xFF0A1524),
                onSurface = RadioTextPrimary,
                surfaceVariant = Color(0xFF11223A),
                onSurfaceVariant = Color(0xFFA1BBDD),
                outline = Color(0xFF1D3A63),
                error = RadioRubyRed
            )
        }
    } else {
        // Light Mode Color Schemes
        when (paletteType) {
            ColorPaletteType.NEON_CYBER -> lightColorScheme(
                primary = Color(0xFF007A8A),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFCCF7FA),
                onPrimaryContainer = Color(0xFF00363D),
                secondary = Color(0xFFC20061),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFD9E6),
                onSecondaryContainer = Color(0xFF3F001D),
                tertiary = Color(0xFF6B00B3),
                background = RadioLightBackground,
                onBackground = RadioLightTextPrimary,
                surface = RadioLightSurface,
                onSurface = RadioLightTextPrimary,
                surfaceVariant = RadioLightCard,
                onSurfaceVariant = RadioLightTextSecondary,
                outline = RadioLightBorder,
                error = RadioRubyRed
            )
            ColorPaletteType.ROYAL_AMBER -> lightColorScheme(
                primary = Color(0xFFB37400),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFECC4),
                onPrimaryContainer = Color(0xFF3B2300),
                secondary = Color(0xFF007B8C),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFBCEEF7),
                onSecondaryContainer = Color(0xFF002026),
                tertiary = Color(0xFFB34A00),
                background = Color(0xFFFCF9F2),
                onBackground = RadioLightTextPrimary,
                surface = RadioLightSurface,
                onSurface = RadioLightTextPrimary,
                surfaceVariant = Color(0xFFF9F5EC),
                onSurfaceVariant = RadioLightTextSecondary,
                outline = Color(0xFFE2D6C0),
                error = RadioRubyRed
            )
            ColorPaletteType.ELECTRIC_EMERALD -> lightColorScheme(
                primary = Color(0xFF008542),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFC7F8DA),
                onPrimaryContainer = Color(0xFF00391A),
                secondary = Color(0xFF007B8C),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFBCEEF7),
                onSecondaryContainer = Color(0xFF002026),
                tertiary = Color(0xFF5A8500),
                background = Color(0xFFF2FBF6),
                onBackground = RadioLightTextPrimary,
                surface = RadioLightSurface,
                onSurface = RadioLightTextPrimary,
                surfaceVariant = Color(0xFFE9F7F0),
                onSurfaceVariant = RadioLightTextSecondary,
                outline = Color(0xFFC2E2D0),
                error = RadioRubyRed
            )
            ColorPaletteType.COSMIC_SUNSET -> lightColorScheme(
                primary = Color(0xFFD61A4E),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFD9E2),
                onPrimaryContainer = Color(0xFF3F0014),
                secondary = Color(0xFFC74E00),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFFFDBC8),
                onSecondaryContainer = Color(0xFF3B1300),
                tertiary = Color(0xFF671B96),
                background = Color(0xFFFDF6F8),
                onBackground = RadioLightTextPrimary,
                surface = RadioLightSurface,
                onSurface = RadioLightTextPrimary,
                surfaceVariant = Color(0xFFF9ECF0),
                onSurfaceVariant = RadioLightTextSecondary,
                outline = Color(0xFFE6CAD2),
                error = RadioRubyRed
            )
            ColorPaletteType.DEEP_SAPPHIRE -> lightColorScheme(
                primary = Color(0xFF0077B6),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFCAE9FF),
                onPrimaryContainer = Color(0xFF00253D),
                secondary = Color(0xFF6A1B9A),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFF3E5F5),
                onSecondaryContainer = Color(0xFF27003D),
                tertiary = Color(0xFF023E8A),
                background = Color(0xFFF4F8FC),
                onBackground = RadioLightTextPrimary,
                surface = RadioLightSurface,
                onSurface = RadioLightTextPrimary,
                surfaceVariant = Color(0xFFEAF1F8),
                onSurfaceVariant = RadioLightTextSecondary,
                outline = Color(0xFFC8D7E6),
                error = RadioRubyRed
            )
        }
    }
}

@Composable
fun MyApplicationTheme(
    themeConfig: ThemeConfig = ThemeConfig(),
    systemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isDark = when (themeConfig.mode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = getCustomColorScheme(
        isDark = isDark,
        paletteType = themeConfig.paletteType
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
