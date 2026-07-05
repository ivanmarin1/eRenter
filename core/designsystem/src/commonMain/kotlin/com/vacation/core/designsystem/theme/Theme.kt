package com.vacation.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightScheme = lightColorScheme()
private val DarkScheme = darkColorScheme()

/** App-wide theme. Wraps Material 3 and provides the extra calendar tokens. */
@Composable
fun VacationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val calendarColors = if (darkTheme) darkCalendarColors() else lightCalendarColors()
    CompositionLocalProvider(LocalCalendarColors provides calendarColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            content = content,
        )
    }
}
