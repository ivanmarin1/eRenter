package com.vacation.app

import androidx.compose.ui.window.ComposeUIViewController
import com.vacation.app.di.initKoin
import com.vacation.feature.calendar.data.db.DatabaseDriverFactory
import platform.UIKit.UIViewController

/** Entry point called from Swift (see iosApp). Starts Koin once, then hosts Compose. */
fun MainViewController(): UIViewController {
    initKoin(DatabaseDriverFactory())
    return ComposeUIViewController { App() }
}
