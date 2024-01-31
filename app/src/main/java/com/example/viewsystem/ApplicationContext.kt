package com.example.viewsystem

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.viewsystem.commons.util.AppForegroundObserver
import com.example.viewsystem.commons.util.AppStartup
import com.example.viewsystem.commons.util.Inspector
import com.example.viewsystem.commons.util.logging.timber.NoopTree
import com.example.viewsystem.core.Env
import com.example.viewsystem.core.di.AppDependencies
import com.example.viewsystem.core.di.AppDependenciesProvider
import com.example.viewsystem.core.di.ApplicationCoroutineScope
import com.example.viewsystem.core.envForConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltAndroidApp
class ApplicationContext : Application(), AppForegroundObserver.Listener, Configuration.Provider {

    val Tag: String = "PepulBusiness"

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject @ApplicationCoroutineScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        AppStartup.getInstance().onApplicationCreate()
        val startTime = System.currentTimeMillis()
        super.onCreate()
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val uid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(BuildConfig.APPLICATION_ID, PackageManager.ApplicationInfoFlags.of(
                PackageManager.GET_META_DATA.toLong())).uid
        } else {
            @Suppress("deprecation")
            packageManager.getApplicationInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_META_DATA).uid
        }

        AndroidThreeTen.init(this)

        AppStartup.getInstance()
            .addBlocking("init-logging", this::initializeLogging)
            .addBlocking("app-dependencies", this::initApplicationDependencies)
            .addBlocking("lifecycle-observer") {
                AppDependencies.appForegroundObserver?.addListener(this)
            }
            // .addBlocking("after-create", this::setupApp)
            .addNonBlocking(this::checkEmulator)
            .addPostRender(this::setupApp)
            .execute()

        Log.d(
            Tag,
            "onCreate() took " + (System.currentTimeMillis() - startTime) + " ms"
        )
    }

    private fun setupApp() {
        // AppDependencies.persistentStore?.getOrCreateDeviceId()
        if (envForConfig(BuildConfig.ENV) != Env.PROD) {
            Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException)
        }
    }

    private fun initApplicationDependencies() {
        AppDependencies.init(
            application = this,
            provider = AppDependenciesProvider(this)
        )
    }

    private fun initializeLogging() {
        when (envForConfig(BuildConfig.ENV)) {
            Env.DEV -> {
                Timber.plant(Timber.DebugTree())
            }
            else -> {
                if (BuildConfig.DEBUG) {
                    Timber.plant(Timber.DebugTree())
                } else {
                    Timber.plant(NoopTree())
                }
            }
        }
    }

    private fun checkEmulator() {
        if (!BuildConfig.DEBUG) {
            envForConfig(BuildConfig.ENV).let { env ->
                if (env == Env.PROD || env == Env.SPECIAL) {
                    if (Inspector.checkEmulatorFiles()) {
                        Log.e(Tag, "Runtime error", RuntimeException("Invalid runtime. Emulator."))
                        exitProcess(-1)
                    }
                }
            }
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    private fun handleUncaughtException(thread: Thread, e: Throwable) {
        Log.e("UncaughtException", "The exception was unhandled", e)
        val result: Writer = StringWriter()
        val printWriter = PrintWriter(result)
        e.printStackTrace(printWriter)
        printWriter.close()
        var arr = e.stackTrace
        val report = StringBuilder(
            """$e""".trimIndent()
        )
        report.append("--------- Stack trace ---------\n\n")
        for (stackTraceElement in arr) {
            report.append("    ").append(stackTraceElement.toString()).append("\n")
        }
        report.append("-------------------------------\n\n")
        val cause: Throwable? = e.cause
        if (cause != null) {
            report.append("--------- Cause ---------\n\n")
            report.append(cause.toString()).append("\n\n")
            arr = cause.stackTrace
            for (stackTraceElement in arr) {
                report.append("    ").append(stackTraceElement.toString()).append("\n")
            }
            report.append("-------------------------------\n\n")
        }
        sendEmail(report.toString())
    }

    private fun sendEmail(crash: String) {
        try {
            val reportContent = """
            |DEVICE OS VERSION CODE: ${Build.VERSION.SDK_INT}
            |DEVICE VERSION CODE NAME: ${Build.VERSION.CODENAME}
            |DEVICE NAME: ${Build.MANUFACTURER} ${Build.MODEL}
            |VERSION CODE: ${BuildConfig.VERSION_CODE}
            |VERSION NAME: ${BuildConfig.VERSION_NAME}
            |PACKAGE NAME: ${BuildConfig.APPLICATION_ID}
            |BUILD TYPE: ${BuildConfig.BUILD_TYPE}

            |$crash
            """.trimIndent().trimMargin()
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            emailIntent.data = Uri.parse("mailto:") // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(CRASH_REPORT_EMAIL))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Crash Report")
            emailIntent.putExtra(Intent.EXTRA_TEXT, reportContent)

            try {
                //start email intent
                val chooser = Intent.createChooser(emailIntent, "Email")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (chooser.resolveActivity(packageManager) != null) {
                    startActivity(chooser)
                    System.exit(1)
                } else {
                    throw IllegalStateException("Cannot perform this action!")
                }
            } catch (e: Exception) {
                //if any thing goes wrong for example no email client application or any exception
                //get and show exception message
                e.printStackTrace()
            }
        } catch (e: Exception) {
            //Timber.tag(TAG).e(TAG, "sendEmail: %s", e.message)
        }
    }

    companion object {
        const val CRASH_REPORT_EMAIL = "support@shopsnearme.co"

        @Volatile
        var currentVisibleScreen: String = ""
            private set

        @JvmName("setCurrentVisibleScreen1")
        @Synchronized
        fun setCurrentVisibleScreen(tag: String) {
            synchronized(this) {
                currentVisibleScreen = tag
            }
        }

        @Synchronized
        fun clearCurrentVisibleScreen() {
            synchronized(this) {
                currentVisibleScreen = ""
            }
        }
    }
}