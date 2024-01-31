package com.example.viewsystem.core.domain.model

import com.google.gson.annotations.SerializedName

data class BaseResponse(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
)