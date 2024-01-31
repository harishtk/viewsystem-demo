package com.example.viewsystem.core.di

import android.app.Application
import com.example.viewsystem.core.util.DefaultPersistentStore
import com.example.viewsystem.commons.util.AppForegroundObserver
import com.example.viewsystem.core.data.persistence.PersistentStore

class AppDependenciesProvider(private val application: Application) : AppDependencies.Provider {
    override fun provideAppForegroundObserver(): AppForegroundObserver {
        return AppForegroundObserver()
    }

    override fun providePersistentStore(): PersistentStore {
        return DefaultPersistentStore.getInstance(application)
    }


}