package com.vacation.feature.calendar.domain.model

import kotlinx.datetime.LocalDate

/**
 * A single reservation for one apartment.
 *
 * Convention: [checkOut] is the departure day (guest leaves in the morning) and
 * [checkIn] is the arrival day (next guest arrives in the afternoon). A day that is
 * simultaneously a check-out for one booking and a check-in for another is a "turnover".
 */
data class Booking(
    val id: BookingId,
    val apartmentId: ApartmentId,
    val guestName: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
) {
    init {
        require(checkOut >= checkIn) { "checkOut ($checkOut) must not be before checkIn ($checkIn)" }
    }
}
