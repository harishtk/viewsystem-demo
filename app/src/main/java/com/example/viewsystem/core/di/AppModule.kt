package com.example.viewsystem.core.di

import android.content.Context
import androidx.work.WorkManager
import com.example.viewsystem.BuildConfig
import com.example.viewsystem.commons.util.Util
import com.example.viewsystem.commons.util.gson.StringConverter
import com.example.viewsystem.commons.util.net.AndroidHeaderInterceptor
import com.example.viewsystem.commons.util.net.ForbiddenInterceptor
import com.example.viewsystem.commons.util.net.GuestUserInterceptor
import com.example.viewsystem.commons.util.net.JwtInterceptor
import com.example.viewsystem.commons.util.net.PlatformInterceptor
import com.example.viewsystem.commons.util.net.UserAgentInterceptor
import com.example.viewsystem.core.Env
import com.example.viewsystem.core.data.persistence.PersistentStore
import com.example.viewsystem.core.envForConfig
import com.example.viewsystem.core.util.DefaultPersistentStore
import com.example.viewsystem.core.util.JsonParser
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class RepositorySource(val repositorySource: RepositorySources)

enum class RepositorySources { Default, RemoteOnly, }

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideOkhttpClient(): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.MINUTES)

        okHttpClientBuilder.addInterceptor(UserAgentInterceptor())
        okHttpClientBuilder.addInterceptor(AndroidHeaderInterceptor())
        okHttpClientBuilder.addInterceptor(JwtInterceptor())
        okHttpClientBuilder.addInterceptor(PlatformInterceptor())
        okHttpClientBuilder.addInterceptor(GuestUserInterceptor())
        okHttpClientBuilder.addInterceptor(ForbiddenInterceptor())

        // Add delays to all api calls
        // ifDebug { okHttpClientBuilder.addInterceptor(DelayInterceptor(2_000, TimeUnit.MILLISECONDS)) }

        if (envForConfig(BuildConfig.ENV) == Env.DEV || BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpClientBuilder.build()
    }

    @Provides
    @Singleton
    @WebService
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(String::class.java, StringConverter())
        .create()

    /* Coroutine scope */
    @ApplicationCoroutineScope
    @Singleton
    @Provides
    fun provideApplicationScope(): CoroutineScope =
        Util.buildCoroutineScope(
            coroutineName = Util.APPLICATION_COROUTINE_NAME
        )
    /* END - Coroutine scope */

    @GsonParser
    @Provides
    fun provideGsonParser(gson: Gson): JsonParser
            = com.example.viewsystem.core.util.GsonParser(gson)

    @Provides
    @Singleton
    fun providePersistentStore(@ApplicationContext application: Context): PersistentStore
        = DefaultPersistentStore.getInstance(application)

    @Provides
    fun provideWorkManager(@ApplicationContext application: Context): WorkManager {
        return WorkManager.getInstance(application)
    }
}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GsonParser

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationCoroutineScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebService