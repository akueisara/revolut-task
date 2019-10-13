package com.example.revoluttask

import android.app.Application
import timber.log.Timber

class RevolutTaskApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}