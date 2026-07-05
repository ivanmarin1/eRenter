package com.vacation.feature.calendar.data.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Creates the platform SQL driver that backs [BookingDatabase]. Each platform provides an
 * `actual`: Android needs a Context, iOS/Desktop create a file-backed database directly.
 */
expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}
