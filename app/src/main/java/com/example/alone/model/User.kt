package com.example.alone.model

data class User(
    val unique_id: String?,
    val username: String?,
    val email: String?,
    val password: String?,
    val profile_name: String?,
    val profile_photo: String? = null,
    val gender: String?,
    val date_of_reg: String? = null,
    val age: Int?
)
