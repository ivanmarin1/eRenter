package com.vacation.app.di

import com.vacation.feature.calendar.data.db.DatabaseDriverFactory
import com.vacation.feature.calendar.data.di.calendarDataModule
import com.vacation.feature.calendar.presentation.di.calendarPresentationModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** All feature Koin modules the app assembles. Adding a new feature = adding its modules here. */
val appModules = listOf(
    calendarDataModule,
    calendarPresentationModule,
)

/**
 * Idempotent so iOS can call it from the entry point without double-starting Koin.
 *
 * The [driverFactory] is created by each platform entry point (Android needs a Context,
 * iOS/Desktop do not) and registered so the data layer can open the local database.
 */
fun initKoin(
    driverFactory: DatabaseDriverFactory,
    appDeclaration: KoinAppDeclaration = {},
) {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            appDeclaration()
            modules(appModules + module { single { driverFactory } })
        }
    }
}
