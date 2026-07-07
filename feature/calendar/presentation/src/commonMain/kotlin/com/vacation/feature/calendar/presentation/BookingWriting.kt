package com.vacation.feature.calendar.presentation

import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.usecase.DetectOverbookingsUseCase

/** Placeholder id for an unsaved draft so it never matches (and excludes) a real booking. */
private const val DRAFT_CANDIDATE_ID = "__candidate__"

/** Builds the persisted [Booking] from a form [BookingDraft], trimming the free-text fields. */
internal fun BookingDraft.toBooking(id: BookingId): Booking =
    Booking(
        id = id,
        apartmentId = apartmentId,
        guestName = guestName.trim(),
        checkIn = checkIn,
        checkOut = checkOut,
        upfrontPayment = upfrontPayment,
        restPayment = restPayment,
        notes = notes.trim(),
        contactInfo = contactInfo.trim(),
        country = country.trim(),
    )

/**
 * Existing reservations that would clash with [draft] for the same apartment, mapped to
 * [BookingSummary] for display. Shared by the calendar and availability editors. [editingId]
 * excludes the booking being edited from clashing with itself.
 */
internal fun DetectOverbookingsUseCase.conflictSummaries(
    draft: BookingDraft,
    editingId: BookingId?,
    existing: List<Booking>,
    apartments: List<Apartment>,
): List<BookingSummary> {
    if (draft.checkOut <= draft.checkIn) return emptyList()
    val candidate = Booking(
        id = editingId ?: BookingId(DRAFT_CANDIDATE_ID),
        apartmentId = draft.apartmentId,
        guestName = draft.guestName,
        checkIn = draft.checkIn,
        checkOut = draft.checkOut,
    )
    val names = apartments.associate { it.id to it.name }
    return conflictsFor(candidate, existing).map { b ->
        BookingSummary(
            bookingId = b.id,
            apartmentId = b.apartmentId,
            apartmentName = names[b.apartmentId] ?: "",
            guestName = b.guestName,
            checkIn = b.checkIn,
            checkOut = b.checkOut,
        )
    }
}
