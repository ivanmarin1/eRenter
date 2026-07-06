package com.vacation.feature.calendar.domain

import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.usecase.DetectOverbookingsUseCase
import com.vacation.feature.calendar.domain.usecase.overlaps
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DetectOverbookingsUseCaseTest {

    private val detect = DetectOverbookingsUseCase()
    private val a1 = ApartmentId("a1")
    private val a2 = ApartmentId("a2")

    private fun booking(id: String, apt: ApartmentId, from: Int, to: Int) =
        Booking(BookingId(id), apt, "Guest $id", LocalDate(2026, 7, from), LocalDate(2026, 7, to))

    @Test
    fun overlappingRangesInSameApartmentConflict() {
        val a = booking("a", a1, 10, 14)
        val b = booking("b", a1, 12, 16)
        assertTrue(a.overlaps(b))
        assertTrue(b.overlaps(a))
    }

    @Test
    fun turnoverIsNotAnOverlap() {
        val leaving = booking("a", a1, 5, 10)
        val arriving = booking("b", a1, 10, 13) // checkout day == next check-in
        assertFalse(leaving.overlaps(arriving))
    }

    @Test
    fun differentApartmentsNeverConflict() {
        val a = booking("a", a1, 10, 14)
        val b = booking("b", a2, 10, 14)
        assertFalse(a.overlaps(b))
    }

    @Test
    fun aBookingDoesNotConflictWithItself() {
        val a = booking("a", a1, 10, 14)
        assertFalse(a.overlaps(a))
    }

    @Test
    fun conflictsForExcludesTheBookingBeingEdited() {
        val existing = listOf(booking("a", a1, 10, 14), booking("b", a1, 20, 24))
        // Re-saving "a" over itself (same id, same span) must not report a self-conflict.
        val candidate = booking("a", a1, 10, 14)
        assertTrue(detect.conflictsFor(candidate, existing).isEmpty())
    }

    @Test
    fun conflictsForFindsOverlappingNeighbour() {
        val existing = listOf(booking("a", a1, 10, 14))
        val candidate = booking("new", a1, 13, 18)
        assertEquals(listOf(BookingId("a")), detect.conflictsFor(candidate, existing).map { it.id })
    }

    @Test
    fun conflictedIdsReturnsBothSidesOfEachOverlap() {
        val bookings = listOf(
            booking("a", a1, 10, 14),
            booking("b", a1, 12, 16), // overlaps a
            booking("c", a1, 20, 24), // isolated
        )
        assertEquals(setOf(BookingId("a"), BookingId("b")), detect.conflictedIds(bookings))
    }

    @Test
    fun conflictingBookingsAreScopedToTheApartmentAndSorted() {
        val bookings = listOf(
            booking("b", a1, 12, 16),
            booking("a", a1, 10, 14),
            booking("other", a2, 11, 13), // different apartment, ignored
        )
        val conflicts = detect.conflictingBookings(bookings, a1)
        assertEquals(listOf(BookingId("a"), BookingId("b")), conflicts.map { it.id })
    }
}
