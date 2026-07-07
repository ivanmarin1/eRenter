package com.vacation.feature.calendar.presentation

import com.vacation.feature.calendar.domain.model.YearMonth
import kotlinx.datetime.LocalDate

/** User intents. The UI only emits these; it never mutates state directly. */
sealed interface CalendarEvent {
    /** In month view moves one month; in year view moves one year. */
    data object PreviousMonth : CalendarEvent
    data object NextMonth : CalendarEvent
    data object GoToToday : CalendarEvent
    data class SelectDay(val date: LocalDate) : CalendarEvent
    data object ClearSelection : CalendarEvent
    data class SetViewMode(val mode: CalendarViewMode) : CalendarEvent
    /** Tap a month in the year overview: jump to it and switch back to month view. */
    data class OpenMonth(val yearMonth: YearMonth) : CalendarEvent
}
