package com.vacation.feature.calendar.domain.usecase

import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId

/**
 * Two bookings overbook when they are for the same apartment and their night ranges overlap.
 * Ranges are half-open `[checkIn, checkOut)`, so a turnover (one guest's check-out equal to the
 * next's check-in) is NOT an overlap. A booking never conflicts with itself.
 */
fun Booking.overlaps(other: Booking): Boolean =
    apartmentId == other.apartmentId &&
        id != other.id &&
        checkIn < other.checkOut &&
        other.checkIn < checkOut

/**
 * Pure overbooking detection, reused for both the hard block on save and for surfacing existing
 * conflicts (apartment-calendar colouring, conflicts list, day-detail badges).
 */
class DetectOverbookingsUseCase {

    /** Existing bookings that would clash with [candidate] (same apartment, overlapping nights). */
    fun conflictsFor(candidate: Booking, existing: List<Booking>): List<Booking> =
        existing.filter { it.overlaps(candidate) }

    /** Ids of every booking that overlaps at least one other booking. */
    fun conflictedIds(bookings: List<Booking>): Set<BookingId> {
        val conflicted = mutableSetOf<BookingId>()
        bookings.groupBy { it.apartmentId }.forEach { (_, group) ->
            for (i in group.indices) {
                for (j in i + 1 until group.size) {
                    if (group[i].overlaps(group[j])) {
                        conflicted += group[i].id
                        conflicted += group[j].id
                    }
                }
            }
        }
        return conflicted
    }

    /** Bookings for [apartmentId] that overlap another booking, ordered by check-in (conflicts list). */
    fun conflictingBookings(bookings: List<Booking>, apartmentId: ApartmentId): List<Booking> {
        val group = bookings.filter { it.apartmentId == apartmentId }
        return group
            .filter { candidate -> group.any { it.overlaps(candidate) } }
            .sortedBy { it.checkIn }
    }
}
