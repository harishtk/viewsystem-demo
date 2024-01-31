package com.example.viewsystem

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU

object VersionCompat

/**
 * Runs the block for devices below [Build.VERSION_CODES.Q]
 */
inline fun sdkBelowQ(block: () -> Unit): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        block()
        return true
    }
    return false
}

/**
 *
 */
inline fun sdkBelowT(block: () -> Unit): Boolean {
    if (SDK_INT < TIRAMISU) {
        block()
        return true
    }
    return false
}

inline fun Boolean.doElse(block: () -> Unit) {
    if (this) {
        block()
    }
}

val VersionCompat.isAtLeastT: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

val VersionCompat.isAtLeastR: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

val VersionCompat.isAtLeastS: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/*inline fun VersionCompat.sdkAtLeastT(block: () -> Boolean): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        block()
        return true
    }
    return false
}*/

/**
 * A utility function to run the [block] for Android version [Build.VERSION_CODES.T] and above.
 *
 * @param block The block to execute.
 * @return R 'null' if the version is below.
 */
inline fun <R> sdkAtLeastT(block: () -> R): R? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        block()
    } else {
        null
    }
}

/**
 * A utility function to run the [block] for Android version [Build.VERSION_CODES.R] and above.
 *
 * @param block The block to execute.
 * @return R 'null' if the version is below.
 */
inline fun <R> sdkAtLeastR(block: () -> R): R? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        block()
    } else {
        null
    }
}