package com.vacation.feature.calendar.domain.model

/**
 * What is happening on a single day of a compact "mini month" (year overview / matrix). One enum
 * covers both the all-apartments schedule view and the per-apartment availability view; each view
 * only produces the subset of kinds that make sense for it.
 */
enum class ScheduleDayKind {
    /** Nothing booked. */
    None,

    /** Occupied for the night, no arrival/departure highlight (or "booked" in availability). */
    Occupied,

    /** A guest checks out this day. */
    Departure,

    /** A guest checks in this day. */
    Arrival,

    /** Same-day check-out and check-in — a turnover/cleaning day. */
    Turnover,

    /** An overbooking conflict (2+ bookings on the same night for one apartment). */
    Conflict,
}

/** One cell of a mini month. [dayOfMonth] is null for the leading blanks before day 1. */
data class MiniMonthCell(
    val dayOfMonth: Int?,
    val kind: ScheduleDayKind,
    val isToday: Boolean,
)

/** A compact month: the [yearMonth] it represents and its day cells (leading blanks included). */
data class MiniMonth(
    val yearMonth: YearMonth,
    val cells: List<MiniMonthCell>,
)
