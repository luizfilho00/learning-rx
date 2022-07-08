package com.example.learningrx01.app

import android.app.Application
import com.example.learningrx01.feature.presentation.di.featureModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            androidLogger(Level.INFO)
            modules(networkModule, featureModule)
        }
    }
}