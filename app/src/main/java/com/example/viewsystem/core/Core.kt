package com.example.viewsystem.core

import androidx.annotation.StringDef
import com.example.viewsystem.Constant

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@StringDef(Constant.ENV_DEV, Constant.ENV_STAGE, Constant.ENV_PROD, Constant.ENV_SPECIAL)
@Retention(AnnotationRetention.SOURCE)
annotation class AppEnv

enum class Env {
    DEV, STAGE, PROD, SPECIAL, INTERNAL
}

fun envForConfig(config: String): Env = when (config) {
    "dev" -> Env.DEV
    "staging" -> Env.STAGE
    "sp" -> Env.SPECIAL
    "prod" -> Env.PROD
    "internal" -> Env.INTERNAL
    else -> throw IllegalStateException("Unknown Environment $config")
}

const val DEFAULT_PAGE_SIZE: Int = 20

/**
 * Represents a No Operation statement
 */
public fun Noop() = Unit