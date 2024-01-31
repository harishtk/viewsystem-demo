package com.example.viewsystem.core.di

import android.app.Application
import android.util.Size
import com.example.viewsystem.commons.util.AppForegroundObserver
import com.example.viewsystem.core.data.persistence.PersistentStore

object AppDependencies {

    @Volatile
    private var application: Application? = null

    @Volatile
    private var provider: Provider? = null

    @Volatile
    var appForegroundObserver: AppForegroundObserver? = null
        get() = field!!

    @Volatile
    var displaySize: Size? = null
        get() {
            if (field == null) {
                throw IllegalStateException(
                    "Tying to access [AppDependencies#displaySize " +
                            "while it's not set."
                )
            }
            return field!!
        }

    @Volatile
    var persistentStore: PersistentStore? = null
        get() = field!!

    @Synchronized
    fun init(application: Application, provider: Provider) {
        synchronized(this) {
            if (AppDependencies.application != null || AppDependencies.provider != null) {
                throw IllegalStateException("Already initialized!")
            }

            AppDependencies.application = application
            AppDependencies.provider = provider
            appForegroundObserver = provider.provideAppForegroundObserver()

            appForegroundObserver!!.begin()
            persistentStore = provider.providePersistentStore()
        }
    }

    interface Provider {
        fun provideAppForegroundObserver(): AppForegroundObserver
        fun providePersistentStore(): PersistentStore
        // fun provideAppWebSocket(): AppWebSocket
    }
}