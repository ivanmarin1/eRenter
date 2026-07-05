package com.vacation.feature.calendar.domain.usecase

import com.vacation.feature.calendar.domain.model.MonthSchedule
import com.vacation.feature.calendar.domain.model.YearMonth
import com.vacation.feature.calendar.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * The single entry point the presentation layer depends on. It reacts to data changes:
 * add a booking and every subscribed month recomputes automatically.
 */
class GetMonthScheduleUseCase(
    private val repository: BookingRepository,
    private val builder: MonthScheduleBuilder = MonthScheduleBuilder(),
) {
    operator fun invoke(yearMonth: YearMonth): Flow<MonthSchedule> =
        combine(
            repository.observeBookings(),
            repository.observeApartments(),
        ) { bookings, apartments ->
            builder.build(yearMonth, bookings, apartments)
        }
}
