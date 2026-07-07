package com.vacation.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * The raw "eRenter" palette, one object per mode. These are the single source of truth for every
 * colour in the app; [CalendarColors] and the Material [ColorScheme] are both derived from them so
 * a rebrand only touches this file. Names match the design's CSS custom properties.
 */
object ERenterLight {
    val bg = Color(0xFFF7F1E7)
    val surface = Color(0xFFFFFFFF)
    val surface2 = Color(0xFFF1E8D8)
    val text = Color(0xFF2E2820)
    val dim = Color(0xFF8B7D6B)
    val border = Color(0xFFE7DCC9)
    val green = Color(0xFF5E7D42)
    val greenSoft = Color(0xFFE4EBD6)
    val amber = Color(0xFFCB8A2C)
    val amberSoft = Color(0xFFF5E7C9)
    val red = Color(0xFFBC4A30)
    val redSoft = Color(0xFFF3D9CF)
    val redStrong = Color(0xFF8E3320)
    val today = Color(0xFF3D6E8C)
    val accent = Color(0xFF8A5A7A)
}

object ERenterDark {
    val bg = Color(0xFF1A1712)
    val surface = Color(0xFF241F18)
    val surface2 = Color(0xFF2E2820)
    val text = Color(0xFFF1E8D9)
    val dim = Color(0xFFA99A86)
    val border = Color(0xFF3A322A)
    val green = Color(0xFF93B377)
    val greenSoft = Color(0xFF33402A)
    val amber = Color(0xFFE1B360)
    val amberSoft = Color(0xFF463A26)
    val red = Color(0xFFE27C60)
    val redSoft = Color(0xFF4A2E28)
    val redStrong = Color(0xFFF0A48E)
    val today = Color(0xFF79A7C4)
    val accent = Color(0xFFC48CB2)
}

private val White = Color(0xFFFFFFFF)

/** Material scheme mapped from the design tokens. "today" is the primary/brand colour. */
fun eRenterLightScheme(): ColorScheme = lightColorScheme(
    primary = ERenterLight.today,
    onPrimary = White,
    primaryContainer = ERenterLight.surface2,
    onPrimaryContainer = ERenterLight.text,
    secondary = ERenterLight.accent,
    onSecondary = White,
    background = ERenterLight.bg,
    onBackground = ERenterLight.text,
    surface = ERenterLight.surface,
    onSurface = ERenterLight.text,
    surfaceVariant = ERenterLight.surface2,
    onSurfaceVariant = ERenterLight.dim,
    outline = ERenterLight.border,
    outlineVariant = ERenterLight.border,
    error = ERenterLight.red,
    onError = White,
    errorContainer = ERenterLight.redSoft,
    onErrorContainer = ERenterLight.redStrong,
)

fun eRenterDarkScheme(): ColorScheme = darkColorScheme(
    primary = ERenterDark.today,
    onPrimary = Color(0xFF10202A),
    primaryContainer = ERenterDark.surface2,
    onPrimaryContainer = ERenterDark.text,
    secondary = ERenterDark.accent,
    onSecondary = Color(0xFF241018),
    background = ERenterDark.bg,
    onBackground = ERenterDark.text,
    surface = ERenterDark.surface,
    onSurface = ERenterDark.text,
    surfaceVariant = ERenterDark.surface2,
    onSurfaceVariant = ERenterDark.dim,
    outline = ERenterDark.border,
    outlineVariant = ERenterDark.border,
    error = ERenterDark.red,
    onError = Color(0xFF241410),
    errorContainer = ERenterDark.redSoft,
    onErrorContainer = ERenterDark.redStrong,
)
