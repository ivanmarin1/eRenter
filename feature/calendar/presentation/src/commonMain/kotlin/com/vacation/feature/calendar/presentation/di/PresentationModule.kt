package com.vacation.feature.calendar.presentation.di

import com.vacation.feature.calendar.domain.usecase.GetMonthScheduleUseCase
import com.vacation.feature.calendar.domain.usecase.MonthScheduleBuilder
import com.vacation.feature.calendar.domain.usecase.ParseBookingImportUseCase
import com.vacation.feature.calendar.presentation.ApartmentsViewModel
import com.vacation.feature.calendar.presentation.CalendarViewModel
import com.vacation.feature.calendar.presentation.ImportViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val calendarPresentationModule = module {
    single { MonthScheduleBuilder() }
    factory { GetMonthScheduleUseCase(repository = get(), builder = get()) }
    factory { ParseBookingImportUseCase() }

    // Provided explicitly because Koin's constructor DSL does not honour Kotlin default args.
    single<Clock> { Clock.System }
    single<TimeZone> { TimeZone.currentSystemDefault() }

    viewModelOf(::CalendarViewModel)
    viewModelOf(::ApartmentsViewModel)
    viewModelOf(::ImportViewModel)
}
