package com.example.alone.model

data class ModerateRequest(
    val text: String
)

data class ModerateResponse(
    val status: String,
    val reason: String? = null)

