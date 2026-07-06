package com.vacation.feature.calendar.domain

import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.ApartmentMonth
import com.vacation.feature.calendar.domain.model.AvailabilityStatus
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.YearMonth
import com.vacation.feature.calendar.domain.usecase.ApartmentAvailabilityBuilder
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class ApartmentAvailabilityBuilderTest {

    private val builder = ApartmentAvailabilityBuilder()
    private val a1 = ApartmentId("a1")
    private val a2 = ApartmentId("a2")
    private val july = YearMonth(2026, Month.JULY)

    private fun ApartmentMonth.statusOn(day: Int): AvailabilityStatus =
        weeks.flatten().first { it.date == LocalDate(2026, 7, day) }.status

    private fun booking(id: String, apt: ApartmentId, from: Int, to: Int) =
        Booking(BookingId(id), apt, "Guest", LocalDate(2026, 7, from), LocalDate(2026, 7, to))

    @Test
    fun occupiedNightsAreBookedAndCheckoutDayIsAvailable() {
        val month = builder.build(a1, july, listOf(booking("a", a1, 10, 13)))
        assertEquals(AvailabilityStatus.Available, month.statusOn(9))
        assertEquals(AvailabilityStatus.Booked, month.statusOn(10))
        assertEquals(AvailabilityStatus.Booked, month.statusOn(12))
        // Check-out day is free for the next arrival.
        assertEquals(AvailabilityStatus.Available, month.statusOn(13))
    }

    @Test
    fun backToBackTurnoverStaysBookedNotOverbooked() {
        val bookings = listOf(booking("a", a1, 5, 10), booking("b", a1, 10, 13))
        val month = builder.build(a1, july, bookings)
        // Day 10 is booked by exactly one booking (the arrival), not two.
        assertEquals(AvailabilityStatus.Booked, month.statusOn(10))
    }

    @Test
    fun overlappingBookingsMarkTheSharedNightsOverbooked() {
        val bookings = listOf(booking("a", a1, 10, 13), booking("b", a1, 11, 14))
        val month = builder.build(a1, july, bookings)
        assertEquals(AvailabilityStatus.Booked, month.statusOn(10))
        assertEquals(AvailabilityStatus.Overbooked, month.statusOn(11))
        assertEquals(AvailabilityStatus.Overbooked, month.statusOn(12))
        assertEquals(AvailabilityStatus.Booked, month.statusOn(13))
    }

    @Test
    fun otherApartmentsBookingsDoNotAffectThisOne() {
        val month = builder.build(a1, july, listOf(booking("x", a2, 10, 20)))
        assertEquals(AvailabilityStatus.Available, month.statusOn(15))
    }
}
