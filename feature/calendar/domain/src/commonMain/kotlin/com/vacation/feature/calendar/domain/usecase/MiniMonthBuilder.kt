package com.vacation.feature.calendar.domain.usecase

import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.MiniMonth
import com.vacation.feature.calendar.domain.model.MiniMonthCell
import com.vacation.feature.calendar.domain.model.ScheduleDayKind
import com.vacation.feature.calendar.domain.model.YearMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

/**
 * Pure builder for the compact "mini months" used by the calendar year overview, the availability
 * year overview and the per-month matrix. Occupancy uses the same half-open `[checkIn, checkOut)`
 * night convention as the rest of the domain, so a check-out day reads as free.
 */
class MiniMonthBuilder(
    private val weekStart: DayOfWeek = DayOfWeek.MONDAY,
) {
    /** The twelve months of [year] coloured by all-apartments activity (arrivals, turnovers, conflicts). */
    fun scheduleYear(year: Int, bookings: List<Booking>, today: LocalDate?): List<MiniMonth> =
        (1..12).map { month -> scheduleMonth(YearMonth(year, monthOf(month)), bookings, today) }

    /** A single all-apartments mini month. */
    fun scheduleMonth(yearMonth: YearMonth, bookings: List<Booking>, today: LocalDate?): MiniMonth =
        MiniMonth(yearMonth, cells(yearMonth, today) { date -> scheduleKind(date, bookings) })

    /** The twelve months of [year] for one apartment, coloured available / booked / overbooked. */
    fun availabilityYear(year: Int, apartmentId: ApartmentId, bookings: List<Booking>, today: LocalDate?): List<MiniMonth> =
        (1..12).map { month -> availabilityMonth(YearMonth(year, monthOf(month)), apartmentId, bookings, today) }

    /** A single per-apartment availability mini month (used by the per-month matrix and year view). */
    fun availabilityMonth(yearMonth: YearMonth, apartmentId: ApartmentId, bookings: List<Booking>, today: LocalDate?): MiniMonth {
        val forApartment = bookings.filter { it.apartmentId == apartmentId }
        return MiniMonth(yearMonth, cells(yearMonth, today) { date -> availabilityKind(date, forApartment) })
    }

    private fun cells(yearMonth: YearMonth, today: LocalDate?, kindOf: (LocalDate) -> ScheduleDayKind): List<MiniMonthCell> {
        val first = yearMonth.firstDay()
        val daysInMonth = first.daysUntil(first.plus(1, DateTimeUnit.MONTH))
        val leadOffset = (first.dayOfWeek.isoDayNumber - weekStart.isoDayNumber + 7) % 7

        val cells = ArrayList<MiniMonthCell>(leadOffset + daysInMonth)
        repeat(leadOffset) { cells += MiniMonthCell(dayOfMonth = null, kind = ScheduleDayKind.None, isToday = false) }
        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            cells += MiniMonthCell(dayOfMonth = day, kind = kindOf(date), isToday = date == today)
        }
        return cells
    }

    private fun scheduleKind(date: LocalDate, bookings: List<Booking>): ScheduleDayKind {
        val occupancyByApartment = bookings
            .filter { it.checkIn <= date && date < it.checkOut }
            .groupingBy { it.apartmentId }
            .eachCount()
        if (occupancyByApartment.any { it.value > 1 }) return ScheduleDayKind.Conflict

        val arrives = bookings.any { it.checkIn == date }
        val departs = bookings.any { it.checkOut == date }
        return when {
            arrives && departs -> ScheduleDayKind.Turnover
            arrives -> ScheduleDayKind.Arrival
            departs -> ScheduleDayKind.Departure
            occupancyByApartment.isNotEmpty() -> ScheduleDayKind.Occupied
            else -> ScheduleDayKind.None
        }
    }

    private fun availabilityKind(date: LocalDate, forApartment: List<Booking>): ScheduleDayKind {
        val occupancy = forApartment.count { it.checkIn <= date && date < it.checkOut }
        return when {
            occupancy == 0 -> ScheduleDayKind.None
            occupancy == 1 -> ScheduleDayKind.Occupied
            else -> ScheduleDayKind.Conflict
        }
    }

    private fun monthOf(month: Int): kotlinx.datetime.Month = kotlinx.datetime.Month(month)
}
