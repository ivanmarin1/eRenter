package com.vacation.feature.calendar.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** A booking event condensed to what the calendar needs to show and edit. */
data class BookingSummary(
    val bookingId: BookingId,
    val apartmentId: ApartmentId,
    val apartmentName: String,
    val guestName: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
)

/** Everything happening on a single day, already resolved (apartment names attached). */
data class DaySchedule(
    val date: LocalDate,
    val inVisibleMonth: Boolean,
    val arrivals: List<BookingSummary>,
    val departures: List<BookingSummary>,
) {
    /** Someone leaves AND someone arrives the same day -> cleaning must happen in between. */
    val isTurnover: Boolean get() = arrivals.isNotEmpty() && departures.isNotEmpty()

    val hasActivity: Boolean get() = arrivals.isNotEmpty() || departures.isNotEmpty()
}

/**
 * A fully computed month grid. The UI only has to render this — no date math, no business rules.
 * [weeks] are rows of exactly 7 days, padded with leading/trailing days from adjacent months.
 */
data class MonthSchedule(
    val yearMonth: YearMonth,
    val weekStart: DayOfWeek,
    val weeks: List<List<DaySchedule>>,
) {
    val days: List<DaySchedule> get() = weeks.flatten()

    fun day(date: LocalDate): DaySchedule? = days.firstOrNull { it.date == date }
}
