package com.vacation.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

/** Generously rounded shapes to match the "eRenter" card / control language. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(11.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(26.dp),
)

/**
 * App-wide theme. Wraps Material 3 with the eRenter palette, Nunito type scale and rounded shapes,
 * and provides the extra calendar tokens. [darkTheme] is hoisted so the in-app ☀/☾ toggle can drive
 * it; it defaults to the system setting.
 */
@Composable
fun VacationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val calendarColors = if (darkTheme) darkCalendarColors() else lightCalendarColors()
    CompositionLocalProvider(LocalCalendarColors provides calendarColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) eRenterDarkScheme() else eRenterLightScheme(),
            typography = appTypography(),
            shapes = AppShapes,
            content = content,
        )
    }
}
