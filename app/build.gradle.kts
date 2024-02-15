import org.jetbrains.kotlin.gradle.utils.toSetOrEmpty

/* Git: Empty commit marker \0 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.nav.safeargs)
}

object Ext {
    const val versionMajor = 0 // Major
    const val versionMinor = 1 // Minor
    const val versionPatch = 1 // Patches, updates
    val versionClassifier: String? = null
    const val versionRevision = "revision-06"
    const val prodRevision = "rc-01"
    const val isSnapshot = false
    const val minSdk = 26
    const val targetSdk = 34
}
android {
    namespace = "com.example.viewsystem"
    compileSdk = Ext.targetSdk

    defaultConfig {
        applicationId = "com.example.viewsystem"
        minSdk = Ext.minSdk
        targetSdk = Ext.targetSdk
        versionCode = generateVersionCode()
        versionName = generateVersionName()
        resourceConfigurations += setOf<String>("en")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"

        freeCompilerArgs +=
            arrayOf(
                "-opt-in=kotlin.RequiresOptIn",
                // Enable experimental coroutines APIs, including Flow
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlin.Experimental",
                "-Xjvm-default=all-compatibility"
            )
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    flavorDimensions.add("default")
    productFlavors {
        create("dev") {
            dimension = "default"

            buildConfigField("String", "BASE_URL", "\"https://seller.hifrds.com\"")
            buildConfigField("String", "API_URL", "\"https://seller.hifrds.com/api/v1/\"")
            buildConfigField("String", "THUMBNAIL_BASE_URL",
                "\"https://d1whtbopipnjq0.cloudfront.net/thumbnail/\""
            )
            buildConfigField("String", "SOCKET_URL", "\"http://43.205.53.52\"")
            buildConfigField("String", "ENV", "\"dev\"")
            buildConfigField("boolean", "IS_SECURED", "false")
            buildConfigField("String", "S3_BUCKET", "\"https://d1whtbopipnjq0.cloudfront.net/\"")
            versionNameSuffix = "-dev${Ext.versionRevision}"
        }

        create("internal") {
            dimension = "default"

            buildConfigField("String", "BASE_URL", "\"https://pepzoondev.hifrds.com\"")
            buildConfigField("String", "API_URL", "\"https://pepzoondev.hifrds.com/api/v4/\"")
            buildConfigField("String", "THUMBNAIL_BASE_URL",
                "\"https://d1whtbopipnjq0.cloudfront.net/thumbnail/\""
            )
            buildConfigField("String", "SOCKET_URL", "\"http://43.205.53.52/\"")
            buildConfigField("String", "ENV", "\"internal\"")
            buildConfigField("boolean", "IS_SECURED", "false")
            buildConfigField("String", "S3_BUCKET", "\"https://d1whtbopipnjq0.cloudfront.net/\"")
            versionNameSuffix = "-internal"
        }

        create("prod") {
            dimension = "default"

            buildConfigField("String", "BASE_URL", "\"https://shops.storesnearme.in\"")
            buildConfigField("String", "API_URL", "\"https://shops.storesnearme.in/api/v3/\"")
            buildConfigField("String", "THUMBNAIL_BASE_URL",
                "\"https://d1whtbopipnjq0.cloudfront.net/liveThumb/\""
            )
            buildConfigField("String", "SOCKET_URL", "\"http://43.205.53.52/\"")
            buildConfigField("String", "ENV", "\"prod\"")
            buildConfigField("boolean", "IS_SECURED", "true")
            buildConfigField("String", "S3_BUCKET", "\"https://d1whtbopipnjq0.cloudfront.net/\"")
        }
    }
    lint {
        abortOnError = false
    }
}

dependencies {

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.asynclayoutinflater)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.metrics.performance)
    implementation(libs.androidx.window)
    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.androidx.recyclerview)

    /* Google */
    implementation(libs.google.material)

    /* Kotlinx Coroutines */
    implementation(libs.kotlinx.coroutines.android)
    /* Kotlinx Serialization */
    implementation(libs.kotlinx.serialization.json)

    /* Hilt */
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation)
    ksp(libs.hilt.android.compiler)

    /* Navigation Components */
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.navigation.ui.ktx)
    implementation(libs.androidx.hilt.navigation)
    implementation(libs.androidx.hilt.navigation.fragment)

    androidTestImplementation(libs.androidx.navigation.testing)

    /* Lifecycle */
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.common)
    implementation(libs.androidx.lifecycle.lifecycle.process)
    implementation(libs.androidx.lifecycle.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.lifecycle.extensions)

    // Work
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    /* Retrofit */
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.okhttp3.logging.interceptor)

    /* Glide */
    implementation(libs.glide)
    implementation(libs.glide.transformations)
    implementation(libs.glide.okhttp3.integration) {
        exclude("glide-parent")
    }
    ksp(libs.glide.compiler)

    /* Timber */
    implementation(libs.timber)

    /* Time Convertor */
    implementation(libs.threetenabp)

    /* External Projects */
    implementation(project(":external:calendarview"))

    // Core library
    testImplementation(libs.junit)
    debugImplementation(testLibs.androidx.testing)
    debugImplementation(testLibs.androidx.arch.testing)
    debugImplementation(testLibs.androidx.fragment.testing)

    // AndroidJUnitRunner and JUnit Rules
    androidTestImplementation(testLibs.androidx.test.runner)
    androidTestImplementation(testLibs.androidx.test.rules)

    // Assertions
    androidTestImplementation(testLibs.androidx.test.junitext)
    androidTestImplementation(testLibs.androidx.test.truthext)
    testImplementation(testLibs.googletruth)

    // Espresso dependencies
    androidTestImplementation(testLibs.androidx.test.espressoCore)
    androidTestImplementation(testLibs.androidx.test.espressoContrib)
    androidTestImplementation(testLibs.androidx.test.espressoIntents)
    androidTestImplementation(testLibs.androidx.test.espressoAccessibility)
    androidTestImplementation(testLibs.androidx.test.espressoWeb)
    androidTestImplementation(testLibs.androidx.test.espressoConcurrent)
    androidTestImplementation(testLibs.androidx.test.espressoIdlingResource)

    // Hilt testing
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)

    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)

    // Coroutines testing
    testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.threetenabp.test) {
        exclude(group = "com.jakewharton.threetenabp", module = "threetenabp")
    }
}

@SuppressWarnings("GrMethodMayBeStatic")
fun generateVersionCode(): Int {
    return Ext.minSdk * 10000000 + Ext.versionMajor * 10000 + Ext.versionMinor * 100 + Ext.versionPatch
}

@SuppressWarnings("GrMethodMayBeStatic")
fun generateVersionName(): String {
    var versionName: String = "${Ext.versionMajor}.${Ext.versionMinor}.${Ext.versionPatch}"
    var versionClassifier: String? = Ext.versionClassifier
    if (Ext.versionClassifier == null && Ext.isSnapshot) {
        versionClassifier = Ext.prodRevision
    }

    if (versionClassifier != null) {
        versionName += "-" + Ext.versionClassifier
    }
    return versionName
}