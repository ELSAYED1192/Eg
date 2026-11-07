package com.example.ui.theme

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val titleAr: String) {
    DARK("الوضع الداكن (Dark)"),
    LIGHT("الوضع الفاتح (Light)"),
    SYSTEM("تلقائي حسب النظام")
}

enum class ColorPaletteType(val titleAr: String, val previewColor1: Color, val previewColor2: Color) {
    NEON_CYBER("نيون سايبر (Neon Cyber)", Color(0xFF00F0FF), Color(0xFFFF007F)),
    ROYAL_AMBER("الكهرمان الملكي (Royal Amber)", Color(0xFFFFB300), Color(0xFF00E5FF)),
    ELECTRIC_EMERALD("الزمرد المشع (Emerald Glow)", Color(0xFF00E676), Color(0xFF00B0FF)),
    COSMIC_SUNSET("شفق الأثير (Cosmic Sunset)", Color(0xFFFF3366), Color(0xFFFF9900)),
    DEEP_SAPPHIRE("الياقوت الأزرق (Deep Sapphire)", Color(0xFF00B4D8), Color(0xFF7209B7))
}

data class ThemeConfig(
    val mode: AppThemeMode = AppThemeMode.DARK,
    val paletteType: ColorPaletteType = ColorPaletteType.NEON_CYBER,
    val customWallpaperUri: String? = null,
    val presetWallpaperIndex: Int = 0, // 0 = Chassis, 1 = Neon Grid, 2 = Vintage Wood, 3 = Cosmic Stars
    val wallpaperBlurDp: Float = 4f,
    val wallpaperDimAlpha: Float = 0.55f
)

object ThemeSettingsRepository {
    private const val PREFS_NAME = "radio_atheer_theme_prefs"
    private const val KEY_MODE = "theme_mode"
    private const val KEY_PALETTE = "palette_type"
    private const val KEY_WALLPAPER_URI = "wallpaper_uri"
    private const val KEY_PRESET_WALLPAPER = "preset_wallpaper"
    private const val KEY_BLUR = "wallpaper_blur"
    private const val KEY_DIM = "wallpaper_dim"

    fun loadThemeConfig(context: Context): ThemeConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val modeStr = prefs.getString(KEY_MODE, AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        val paletteStr = prefs.getString(KEY_PALETTE, ColorPaletteType.NEON_CYBER.name) ?: ColorPaletteType.NEON_CYBER.name
        val uriStr = prefs.getString(KEY_WALLPAPER_URI, null)
        val preset = prefs.getInt(KEY_PRESET_WALLPAPER, 0)
        val blur = prefs.getFloat(KEY_BLUR, 4f)
        val dim = prefs.getFloat(KEY_DIM, 0.55f)

        val mode = try { AppThemeMode.valueOf(modeStr) } catch (e: Exception) { AppThemeMode.DARK }
        val palette = try { ColorPaletteType.valueOf(paletteStr) } catch (e: Exception) { ColorPaletteType.NEON_CYBER }

        return ThemeConfig(
            mode = mode,
            paletteType = palette,
            customWallpaperUri = uriStr,
            presetWallpaperIndex = preset,
            wallpaperBlurDp = blur,
            wallpaperDimAlpha = dim
        )
    }

    fun saveThemeConfig(context: Context, config: ThemeConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_MODE, config.mode.name)
            .putString(KEY_PALETTE, config.paletteType.name)
            .putString(KEY_WALLPAPER_URI, config.customWallpaperUri)
            .putInt(KEY_PRESET_WALLPAPER, config.presetWallpaperIndex)
            .putFloat(KEY_BLUR, config.wallpaperBlurDp)
            .putFloat(KEY_DIM, config.wallpaperDimAlpha)
            .apply()
    }
}
