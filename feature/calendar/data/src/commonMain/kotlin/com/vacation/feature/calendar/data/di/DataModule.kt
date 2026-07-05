package com.vacation.feature.calendar.data.di

import com.vacation.feature.calendar.data.repository.DefaultBookingRepository
import com.vacation.feature.calendar.data.source.BookingLocalDataSource
import com.vacation.feature.calendar.data.source.SqlDelightBookingDataSource
import com.vacation.feature.calendar.domain.repository.BookingRepository
import org.koin.dsl.module

/**
 * Wires the data layer. The [com.vacation.feature.calendar.data.db.DatabaseDriverFactory] is
 * supplied per platform when Koin is started (it needs a Context on Android), so it is only
 * resolved here via `get()`.
 */
val calendarDataModule = module {
    single<BookingLocalDataSource> { SqlDelightBookingDataSource(get()) }
    single<BookingRepository> { DefaultBookingRepository(get()) }
}
