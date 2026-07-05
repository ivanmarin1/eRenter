package com.vacation.feature.calendar.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/** iOS driver. Schema creation/migration is handled by [NativeSqliteDriver]. */
actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver =
        NativeSqliteDriver(BookingDatabase.Schema, "booking.db")
}
