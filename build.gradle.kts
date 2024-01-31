// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://repo1.maven.org/maven2")
    }

    dependencies {
        classpath(libs.android.gradlePlugin)
        classpath(libs.google.services.gradlePlugin)
        classpath(libs.kotlin.gradlePlugin)
        classpath(libs.ksp.gradlePlugin)
        classpath(libs.hilt.android.gradlePlugin)
        classpath(libs.androidx.navigation.safeargs.gradlePlugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply(false)
    alias(libs.plugins.android.library) apply(false)
    alias(libs.plugins.android.nav.safeargs) apply(false)
    alias(libs.plugins.android.test) apply(false)
    alias(libs.plugins.protobuf) apply(false)
    alias(libs.plugins.hilt) apply(false)
    alias(libs.plugins.kapt) apply(false)
    alias(libs.plugins.ksp) apply(false)
    alias(libs.plugins.kotlin) apply(false)
    alias(libs.plugins.kotlin.jvm) apply(false)
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
