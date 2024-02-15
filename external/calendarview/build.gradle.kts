plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.haibin.calendarview"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    defaultConfig {
        minSdk = 14
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
            )
        }
    }
    /*task intoJar(type: Copy) {
        delete 'build/libs/CalendarView.jar'
        from('build/intermediates/bundles/release/')
        into('build/libs/')
        include('classes.jar')
        rename ('classes.jar', 'CalendarView.jar')
    }
    intoJar.dependsOn(build)*/
}

dependencies {
    // implementation fileTree(dir: 'libs', include: ['*.jar'])

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)

    testImplementation(libs.junit)

    // Espresso dependencies
    androidTestImplementation(testLibs.androidx.test.espressoCore)
}