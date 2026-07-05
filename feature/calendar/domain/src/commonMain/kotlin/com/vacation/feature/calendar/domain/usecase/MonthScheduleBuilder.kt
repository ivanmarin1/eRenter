package com.vacation.feature.calendar.domain.usecase

import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.model.DaySchedule
import com.vacation.feature.calendar.domain.model.MonthSchedule
import com.vacation.feature.calendar.domain.model.YearMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

/**
 * Pure function that turns raw bookings into a ready-to-render month grid.
 * No coroutines, no I/O, no framework types — trivially unit-testable and reusable.
 */
class MonthScheduleBuilder(
    private val weekStart: DayOfWeek = DayOfWeek.MONDAY,
) {
    fun build(
        yearMonth: YearMonth,
        bookings: List<Booking>,
        apartments: List<Apartment>,
    ): MonthSchedule {
        val names = apartments.associate { it.id to it.name }
        val arrivalsByDate = bookings.groupBy { it.checkIn }
        val departuresByDate = bookings.groupBy { it.checkOut }

        val firstOfMonth = yearMonth.firstDay()
        val firstOfNext = firstOfMonth.plus(1, DateTimeUnit.MONTH)
        val daysInMonth = firstOfMonth.daysUntil(firstOfNext)

        // How many trailing days of the previous month we render before day 1.
        val leadOffset = (firstOfMonth.dayOfWeek.isoDayNumber - weekStart.isoDayNumber + 7) % 7
        val gridStart = firstOfMonth.plus(-leadOffset, DateTimeUnit.DAY)
        val totalCells = ((leadOffset + daysInMonth + 6) / 7) * 7 // round up to full weeks

        fun summarize(list: List<Booking>?): List<BookingSummary> =
            list.orEmpty()
                .map { b ->
                    BookingSummary(
                        bookingId = b.id,
                        apartmentId = b.apartmentId,
                        apartmentName = names[b.apartmentId] ?: "Unknown apartment",
                        guestName = b.guestName,
                    )
                }
                .sortedBy { it.apartmentName }

        val days = (0 until totalCells).map { offset ->
            val date = gridStart.plus(offset, DateTimeUnit.DAY)
            DaySchedule(
                date = date,
                inVisibleMonth = date.month == yearMonth.month && date.year == yearMonth.year,
                arrivals = summarize(arrivalsByDate[date]),
                departures = summarize(departuresByDate[date]),
            )
        }

        return MonthSchedule(
            yearMonth = yearMonth,
            weekStart = weekStart,
            weeks = days.chunked(7),
        )
    }
}
