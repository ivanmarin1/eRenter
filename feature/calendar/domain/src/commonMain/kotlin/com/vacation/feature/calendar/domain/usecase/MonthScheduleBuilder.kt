package com.vacation.feature.calendar.domain.usecase

import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.model.DaySchedule
import com.vacation.feature.calendar.domain.model.MonthSchedule
import com.vacation.feature.calendar.domain.model.YearMonth
import kotlinx.datetime.DayOfWeek

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

        fun summarize(list: List<Booking>?): List<BookingSummary> =
            list.orEmpty()
                .map { b ->
                    BookingSummary(
                        bookingId = b.id,
                        apartmentId = b.apartmentId,
                        apartmentName = names[b.apartmentId] ?: "Unknown apartment",
                        guestName = b.guestName,
                        checkIn = b.checkIn,
                        checkOut = b.checkOut,
                        upfrontPayment = b.upfrontPayment,
                        restPayment = b.restPayment,
                        notes = b.notes,
                        contactInfo = b.contactInfo,
                        country = b.country,
                    )
                }
                .sortedBy { it.apartmentName }

        val days = monthGridDates(yearMonth, weekStart).map { gridDay ->
            DaySchedule(
                date = gridDay.date,
                inVisibleMonth = gridDay.inVisibleMonth,
                arrivals = summarize(arrivalsByDate[gridDay.date]),
                departures = summarize(departuresByDate[gridDay.date]),
            )
        }

        return MonthSchedule(
            yearMonth = yearMonth,
            weekStart = weekStart,
            weeks = days.chunked(7),
        )
    }
}
