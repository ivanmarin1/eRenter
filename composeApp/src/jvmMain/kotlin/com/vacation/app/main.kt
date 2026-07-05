package com.vacation.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.vacation.app.di.initKoin
import com.vacation.feature.calendar.data.db.DatabaseDriverFactory

fun main() {
    initKoin(DatabaseDriverFactory())
    application {
        Window(onCloseRequest = ::exitApplication, title = "Booking Calendar") {
            App()
        }
    }
}
