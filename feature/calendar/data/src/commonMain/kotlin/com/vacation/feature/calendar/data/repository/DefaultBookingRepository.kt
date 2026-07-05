package com.vacation.feature.calendar.data.repository

import com.vacation.feature.calendar.data.source.BookingLocalDataSource
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Thin implementation of the domain contract. Today it just forwards to a local source;
 * later it can coordinate cache + network, mapping, conflict resolution, etc.
 */
class DefaultBookingRepository(
    private val local: BookingLocalDataSource,
) : BookingRepository {
    override fun observeApartments(): Flow<List<Apartment>> = local.apartments()
    override fun observeBookings(): Flow<List<Booking>> = local.bookings()
    override suspend fun upsertApartment(apartment: Apartment) = local.upsertApartment(apartment)
    override suspend fun deleteApartment(apartmentId: ApartmentId) = local.deleteApartment(apartmentId)
    override suspend fun upsertBooking(booking: Booking) = local.upsert(booking)
    override suspend fun deleteBooking(bookingId: BookingId) = local.delete(bookingId)
}
