package com.oliveracing.rallycodriver

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    // Provides the Android Application Context
    single { androidContext() } // Koin's way to provide Context, often used as single

    // Factory for SpeechRecognizerUtil - a new instance created each time it's requested
    // It depends on the Application Context
    factory { SpeechRecognizerUtil(androidContext()) }

    // Singleton for PaceNoteParser - only one instance will be created and shared
    // Since PaceNoteParser is an object, Koin will just manage this existing instance.
    single { PaceNoteParser }

    // ViewModel for RallyScreen
    viewModel { RallyViewModel(get()) } // "get()" will resolve PaceNoteParser
}

class RallyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Log Koin activity - useful for debugging
            androidLogger() // Use androidLogger for Android projects
            // Declare Android context
            androidContext(this@RallyApplication)
            // Declare modules to use
            modules(appModule)
        }
    }
}
