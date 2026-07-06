package com.vacation.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic calendar tokens live in the design system, not in the UI. Renaming or
 * rethemeing the calendar (or reusing it in another app with a different palette)
 * happens here — the day cell only ever asks for "departureContainer", never a hex value.
 *
 * Containers are pale so the day number stays readable; the strong variants are used for
 * the legend and detail accents.
 */
data class CalendarColors(
    val departure: Color,
    val departureContainer: Color,
    val arrival: Color,
    val arrivalContainer: Color,
    val turnoverAccent: Color,
    val todayRing: Color,
    val outsideMonth: Color,
    val gridLine: Color,
    // Per-apartment availability calendar: whole-day fills for free / booked / overbooked.
    val available: Color,
    val availableContainer: Color,
    val booked: Color,
    val bookedContainer: Color,
    val overbooked: Color,
    val overbookedContainer: Color,
)

fun lightCalendarColors(): CalendarColors = CalendarColors(
    departure = Color(0xFFB26A00),          // amber – guest leaving
    departureContainer = Color(0xFFFFE7C2),
    arrival = Color(0xFF1F7A4D),            // green – guest arriving
    arrivalContainer = Color(0xFFCDEFD9),
    turnoverAccent = Color(0xFFB3261E),     // red – cleaning needed same day
    todayRing = Color(0xFF3D5AFE),
    outsideMonth = Color(0xFFB0B4BA),
    gridLine = Color(0xFFE6E8EC),
    available = Color(0xFF1F7A4D),
    availableContainer = Color(0xFFCDEFD9), // pale green – free to book
    booked = Color(0xFF8C1D18),
    bookedContainer = Color(0xFFF6D3D0),    // pale red – occupied
    overbooked = Color(0xFFFFFFFF),
    overbookedContainer = Color(0xFFCF2318), // strong red – overbooking conflict
)

fun darkCalendarColors(): CalendarColors = CalendarColors(
    departure = Color(0xFFF2B25C),
    departureContainer = Color(0xFF4A3410),
    arrival = Color(0xFF7CCF9E),
    arrivalContainer = Color(0xFF123A26),
    turnoverAccent = Color(0xFFF2857C),
    todayRing = Color(0xFF8C9EFF),
    outsideMonth = Color(0xFF5A5F66),
    gridLine = Color(0xFF2A2E33),
    available = Color(0xFF7CCF9E),
    availableContainer = Color(0xFF163A28), // deep green – free to book
    booked = Color(0xFFF2B4AE),
    bookedContainer = Color(0xFF541A15),    // deep red – occupied
    overbooked = Color(0xFFFFFFFF),
    overbookedContainer = Color(0xFFB0261C), // strong red – overbooking conflict
)

val LocalCalendarColors = staticCompositionLocalOf { lightCalendarColors() }

/** Access point: `VacationDesign.calendarColors`. */
object VacationDesign {
    val calendarColors: CalendarColors
        @Composable @ReadOnlyComposable get() = LocalCalendarColors.current
}
