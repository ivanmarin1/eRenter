package com.vacation.feature.calendar.domain.usecase

import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.ApartmentMonth
import com.vacation.feature.calendar.domain.model.AvailabilityStatus
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.DayAvailability
import com.vacation.feature.calendar.domain.model.YearMonth
import kotlinx.datetime.DayOfWeek

/**
 * Pure function: turns bookings into a per-apartment month of available/booked/overbooked days.
 *
 * A day is occupied when a booking spans it as a night, i.e. `checkIn <= day < checkOut`. The
 * check-out day itself is free (so back-to-back turnovers read as available). The number of
 * bookings occupying a night gives the status: 0 available, 1 booked, 2+ overbooked.
 */
class ApartmentAvailabilityBuilder(
    private val weekStart: DayOfWeek = DayOfWeek.MONDAY,
) {
    fun build(
        apartmentId: ApartmentId,
        yearMonth: YearMonth,
        bookings: List<Booking>,
    ): ApartmentMonth {
        val forApartment = bookings.filter { it.apartmentId == apartmentId }

        val days = monthGridDates(yearMonth, weekStart).map { gridDay ->
            val occupancy = forApartment.count { it.checkIn <= gridDay.date && gridDay.date < it.checkOut }
            DayAvailability(
                date = gridDay.date,
                inVisibleMonth = gridDay.inVisibleMonth,
                status = when {
                    occupancy == 0 -> AvailabilityStatus.Available
                    occupancy == 1 -> AvailabilityStatus.Booked
                    else -> AvailabilityStatus.Overbooked
                },
            )
        }

        return ApartmentMonth(
            apartmentId = apartmentId,
            yearMonth = yearMonth,
            weekStart = weekStart,
            weeks = days.chunked(7),
        )
    }
}
