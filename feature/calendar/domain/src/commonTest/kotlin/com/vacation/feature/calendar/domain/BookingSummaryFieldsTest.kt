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

class BookingSummaryFieldsTest {

    @Test
    fun builderCarriesPaymentContactAndNotesIntoTheSummary() {
        val apt = Apartment(ApartmentId("a1"), "Sea View")
        val booking = Booking(
            id = BookingId("b1"),
            apartmentId = apt.id,
            guestName = "Ivić",
            checkIn = LocalDate(2026, 7, 3),
            checkOut = LocalDate(2026, 7, 8),
            upfrontPayment = 150.0,
            restPayment = 50.0,
            notes = "Late arrival",
            contactInfo = "+385 91 000",
            country = "Croatia",
        )

        val schedule = MonthScheduleBuilder().build(YearMonth(2026, Month.JULY), listOf(booking), listOf(apt))
        val summary = schedule.day(LocalDate(2026, 7, 3))!!.arrivals.single()

        assertEquals(150.0, summary.upfrontPayment)
        assertEquals(50.0, summary.restPayment)
        assertEquals("Late arrival", summary.notes)
        assertEquals("+385 91 000", summary.contactInfo)
        assertEquals("Croatia", summary.country)
    }
}
