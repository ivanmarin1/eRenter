package com.vacation.feature.calendar.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties

/**
 * Desktop driver. Stores the database as a file under the user's home directory so data
 * survives restarts. Passing the schema to [JdbcSqliteDriver] lets it create the schema on a
 * fresh file and run the `.sqm` migrations on an existing one (tracked via `user_version`),
 * matching the automatic create/migrate the Android and iOS drivers already do.
 */
actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver {
        val dir = File(System.getProperty("user.home"), ".bookingcalendar").apply { mkdirs() }
        val dbFile = File(dir, "booking.db")
        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            properties = Properties(),
            schema = BookingDatabase.Schema,
        )
    }
}
