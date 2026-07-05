package com.vacation.feature.calendar.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

/**
 * Desktop driver. Stores the database as a file under the user's home directory so data
 * survives restarts. Unlike the Android/iOS drivers, the JDBC driver does not auto-create
 * the schema, so we do it once when the file does not yet exist.
 */
actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        val dir = File(System.getProperty("user.home"), ".bookingcalendar").apply { mkdirs() }
        val dbFile = File(dir, "booking.db")
        val freshDatabase = !dbFile.exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        if (freshDatabase) {
            BookingDatabase.Schema.create(driver)
        }
        return driver
    }
}
