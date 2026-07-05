package com.vacation.feature.calendar.domain.repository

import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import kotlinx.coroutines.flow.Flow

/**
 * The domain's contract for booking data. It is deliberately storage-agnostic:
 * the current in-memory source, a future SQLDelight cache, or a remote backend can all
 * satisfy this without the domain or UI changing.
 *
 * When the larger booking app arrives, it can either implement this same interface or
 * expose a richer one that extends it.
 */
interface BookingRepository {
    fun observeApartments(): Flow<List<Apartment>>
    fun observeBookings(): Flow<List<Booking>>

    /** Insert or rename an apartment (matched by [Apartment.id]). */
    suspend fun upsertApartment(apartment: Apartment)

    /** Remove an apartment and all of its bookings. */
    suspend fun deleteApartment(apartmentId: ApartmentId)

    suspend fun upsertBooking(booking: Booking)
    suspend fun deleteBooking(bookingId: BookingId)
}
