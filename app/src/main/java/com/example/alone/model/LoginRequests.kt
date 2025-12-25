package com.example.alone.model

data class LoginWithUsernameRequest(
    val username: String,
    val password: String
)

data class LoginWithEmailRequest(
    val email: String,
    val password: String
)
