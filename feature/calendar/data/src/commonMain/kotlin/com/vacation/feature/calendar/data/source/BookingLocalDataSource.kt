package com.vacation.feature.calendar.data.source

import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import kotlinx.coroutines.flow.Flow

/**
 * The seam that hides *where* data lives. Swap [InMemoryBookingDataSource] for a
 * SQLDelight- or Ktor-backed implementation later without touching the repository,
 * domain, presentation, or UI.
 */
interface BookingLocalDataSource {
    fun apartments(): Flow<List<Apartment>>
    fun bookings(): Flow<List<Booking>>
    suspend fun upsertApartment(apartment: Apartment)
    suspend fun deleteApartment(id: ApartmentId)
    suspend fun upsert(booking: Booking)
    suspend fun delete(id: BookingId)
}
