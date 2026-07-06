package com.vacation.feature.calendar.presentation

import com.vacation.feature.calendar.domain.model.ApartmentId
import kotlinx.datetime.LocalDate

/**
 * Everything the add/edit reservation form collects. Passing this single value (rather than a
 * long parameter list) keeps the ViewModel and UI signatures stable as new booking fields are
 * added over time — a new field is one property here, not a change to every call site.
 */
data class BookingDraft(
    val apartmentId: ApartmentId,
    val guestName: String,
    val contactInfo: String,
    val country: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val upfrontPayment: Double?,
    val restPayment: Double?,
    val notes: String,
)
