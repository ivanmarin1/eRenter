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
 * The palette mirrors the "eRenter" design system: a warm cream/earth light theme and a
 * warm charcoal dark theme, with muted sage (arrival), amber (departure), terracotta
 * (booked/overbooked) and a teal "today" accent.
 *
 * Containers are pale so the day number stays readable; the strong variants are used for
 * whole-day fills, the legend and detail accents.
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
    departure = ERenterLight.amber,             // amber – guest leaving
    departureContainer = ERenterLight.amberSoft,
    arrival = ERenterLight.green,               // green – guest arriving
    arrivalContainer = ERenterLight.greenSoft,
    turnoverAccent = ERenterLight.accent,       // plum – cleaning needed same day
    todayRing = ERenterLight.today,
    outsideMonth = ERenterLight.dim,
    gridLine = ERenterLight.border,
    available = ERenterLight.green,
    availableContainer = ERenterLight.greenSoft, // pale green – free to book
    booked = Color(0xFFFFFFFF),                  // white number on a solid red fill
    bookedContainer = ERenterLight.red,          // solid red – occupied
    overbooked = Color(0xFFFFFFFF),
    overbookedContainer = ERenterLight.redStrong, // strong red – overbooking conflict
)

fun darkCalendarColors(): CalendarColors = CalendarColors(
    departure = ERenterDark.amber,
    departureContainer = ERenterDark.amberSoft,
    arrival = ERenterDark.green,
    arrivalContainer = ERenterDark.greenSoft,
    turnoverAccent = ERenterDark.accent,
    todayRing = ERenterDark.today,
    outsideMonth = ERenterDark.dim,
    gridLine = ERenterDark.border,
    available = ERenterDark.green,
    availableContainer = ERenterDark.greenSoft,
    booked = Color(0xFFFFFFFF),
    bookedContainer = ERenterDark.red,
    overbooked = Color(0xFFFFFFFF),
    overbookedContainer = ERenterDark.redStrong,
)

val LocalCalendarColors = staticCompositionLocalOf { lightCalendarColors() }

/** Access point: `VacationDesign.calendarColors`. */
object VacationDesign {
    val calendarColors: CalendarColors
        @Composable @ReadOnlyComposable get() = LocalCalendarColors.current
}
