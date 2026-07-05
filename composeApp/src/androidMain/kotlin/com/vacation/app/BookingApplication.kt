package com.vacation.app

import android.app.Application
import com.vacation.app.di.initKoin
import com.vacation.feature.calendar.data.db.DatabaseDriverFactory

class BookingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(DatabaseDriverFactory(this))
    }
}
