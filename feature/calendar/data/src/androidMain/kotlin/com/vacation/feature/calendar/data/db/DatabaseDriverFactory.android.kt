package com.vacation.feature.calendar.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/** Android driver. Schema creation/migration is handled by [AndroidSqliteDriver]. */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(BookingDatabase.Schema, context, "booking.db")
}
