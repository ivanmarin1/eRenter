package com.vacation.feature.calendar.data.source

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.vacation.feature.calendar.data.db.ApartmentEntity
import com.vacation.feature.calendar.data.db.BookingDatabase
import com.vacation.feature.calendar.data.db.BookingEntity
import com.vacation.feature.calendar.data.db.DatabaseDriverFactory
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * SQLDelight-backed store. This is the real source of truth: apartments and bookings the
 * host creates/imports/edits persist to a local database and survive restarts. The database
 * starts empty — nothing is seeded — so the same app serves any host.
 */
class SqlDelightBookingDataSource(
    driverFactory: DatabaseDriverFactory,
) : BookingLocalDataSource {

    private val database = BookingDatabase(driverFactory.create())
    private val apartmentQueries = database.apartmentQueries
    private val bookingQueries = database.bookingQueries

    override fun apartments(): Flow<List<Apartment>> =
        apartmentQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    override fun bookings(): Flow<List<Booking>> =
        bookingQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun upsertApartment(apartment: Apartment) = withContext(Dispatchers.Default) {
        // Preserve existing order on rename; append new apartments to the end.
        val order = apartmentQueries.selectAll().executeAsList()
            .firstOrNull { it.id == apartment.id.value }
            ?.sortOrder
            ?: apartmentQueries.nextSortOrder().executeAsOne()
        apartmentQueries.upsert(apartment.id.value, apartment.name, order)
    }

    override suspend fun deleteApartment(id: ApartmentId) = withContext(Dispatchers.Default) {
        database.transaction {
            bookingQueries.deleteByApartment(id.value)
            apartmentQueries.deleteById(id.value)
        }
    }

    override suspend fun upsert(booking: Booking) = withContext(Dispatchers.Default) {
        bookingQueries.upsert(
            id = booking.id.value,
            apartmentId = booking.apartmentId.value,
            guestName = booking.guestName,
            checkIn = booking.checkIn.toString(),
            checkOut = booking.checkOut.toString(),
            upfrontPayment = booking.upfrontPayment,
            restPayment = booking.restPayment,
            notes = booking.notes,
            contactInfo = booking.contactInfo,
            country = booking.country,
        )
    }

    override suspend fun delete(id: BookingId) = withContext(Dispatchers.Default) {
        bookingQueries.deleteById(id.value)
    }
}

private fun ApartmentEntity.toDomain(): Apartment =
    Apartment(id = ApartmentId(id), name = name)

private fun BookingEntity.toDomain(): Booking =
    Booking(
        id = BookingId(id),
        apartmentId = ApartmentId(apartmentId),
        guestName = guestName,
        checkIn = LocalDate.parse(checkIn),
        checkOut = LocalDate.parse(checkOut),
        upfrontPayment = upfrontPayment,
        restPayment = restPayment,
        notes = notes,
        contactInfo = contactInfo,
        country = country,
    )
