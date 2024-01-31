package com.example.viewsystem.commons.util

import com.example.viewsystem.commons.util.UiText

data class ValidationResult(
    val typedValue: String, /* for StateFlow to recognize different value */
    val successful: Boolean = false,
    val errorMessage: UiText? = null
)