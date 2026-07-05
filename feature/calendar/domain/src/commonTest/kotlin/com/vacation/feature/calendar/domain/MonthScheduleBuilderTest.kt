package com.vacation.feature.calendar.domain

import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.YearMonth
import com.vacation.feature.calendar.domain.usecase.MonthScheduleBuilder
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MonthScheduleBuilderTest {

    private val apt = Apartment(ApartmentId("a1"), "Sea View")
    private val builder = MonthScheduleBuilder()

    @Test
    fun detectsTurnoverWhenDepartureAndArrivalShareADay() {
        val turnover = LocalDate(2026, 7, 10)
        val leaving = Booking(BookingId("b1"), apt.id, "Ivić", LocalDate(2026, 7, 3), turnover)
        val arriving = Booking(BookingId("b2"), apt.id, "Kovač", turnover, LocalDate(2026, 7, 18))

        val schedule = builder.build(YearMonth(2026, Month.JULY), listOf(leaving, arriving), listOf(apt))
        val day = schedule.day(turnover)!!

        assertTrue(day.isTurnover, "10 July should be a turnover day")
        assertEquals("Ivić", day.departures.single().guestName)
        assertEquals("Kovač", day.arrivals.single().guestName)
    }

    @Test
    fun gridAlwaysContainsFullWeeks() {
        val schedule = builder.build(YearMonth(2026, Month.FEBRUARY), emptyList(), listOf(apt))
        assertTrue(schedule.weeks.all { it.size == 7 })
    }

    @Test
    fun resolvesApartmentNames() {
        val b = Booking(BookingId("b1"), apt.id, "Horvat", LocalDate(2026, 7, 5), LocalDate(2026, 7, 9))
        val schedule = builder.build(YearMonth(2026, Month.JULY), listOf(b), listOf(apt))
        assertEquals("Sea View", schedule.day(LocalDate(2026, 7, 5))!!.arrivals.single().apartmentName)
    }
}
