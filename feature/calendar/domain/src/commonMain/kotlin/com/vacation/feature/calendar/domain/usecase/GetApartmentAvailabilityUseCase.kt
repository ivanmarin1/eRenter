package com.vacation.feature.calendar.domain.usecase

import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.ApartmentMonth
import com.vacation.feature.calendar.domain.model.YearMonth
import com.vacation.feature.calendar.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reactive per-apartment availability for a month. Re-emits whenever bookings change, so adding
 * or editing a reservation instantly recolors the availability calendar.
 */
class GetApartmentAvailabilityUseCase(
    private val repository: BookingRepository,
    private val builder: ApartmentAvailabilityBuilder = ApartmentAvailabilityBuilder(),
) {
    operator fun invoke(apartmentId: ApartmentId, yearMonth: YearMonth): Flow<ApartmentMonth> =
        repository.observeBookings().map { bookings ->
            builder.build(apartmentId, yearMonth, bookings)
        }
}
