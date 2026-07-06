package com.vacation.feature.calendar.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** Occupancy of a single day for one apartment. */
enum class AvailabilityStatus {
    /** No booking occupies this night — free to book (green). */
    Available,

    /** Exactly one booking occupies this night (red). */
    Booked,

    /** Two or more bookings occupy this night — an overbooking conflict (stronger red). */
    Overbooked,
}

/** A day in the per-apartment availability grid. */
data class DayAvailability(
    val date: LocalDate,
    val inVisibleMonth: Boolean,
    val status: AvailabilityStatus,
)

/**
 * A fully computed availability month for one apartment. [weeks] are rows of exactly 7 days,
 * padded with leading/trailing days from adjacent months (same grid as [MonthSchedule]).
 */
data class ApartmentMonth(
    val apartmentId: ApartmentId,
    val yearMonth: YearMonth,
    val weekStart: DayOfWeek,
    val weeks: List<List<DayAvailability>>,
)
