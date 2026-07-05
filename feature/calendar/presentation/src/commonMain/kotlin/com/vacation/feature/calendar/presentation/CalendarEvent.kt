package com.vacation.feature.calendar.presentation

import kotlinx.datetime.LocalDate

/** User intents. The UI only emits these; it never mutates state directly. */
sealed interface CalendarEvent {
    data object PreviousMonth : CalendarEvent
    data object NextMonth : CalendarEvent
    data object GoToToday : CalendarEvent
    data class SelectDay(val date: LocalDate) : CalendarEvent
    data object ClearSelection : CalendarEvent
}
