package com.example.viewsystem.feature.inputsheet

import com.example.viewsystem.core.designsystem.component.text.EditTextState
import kotlinx.coroutines.flow.update

class MessageTextState(
    private val initialValue: String = ""
) : EditTextState(validator = ::isMessageValid, errorFor = ::errorForMessage) {
    init {
        this.text.update { initialValue }
    }
}

private fun isMessageValid(text: String): Boolean {
    return text.isNotBlank()
}

private fun errorForMessage(text: String): String {
    return when {
        !isMessageValid(text) -> "Field is required"
        else -> ""
    }
}