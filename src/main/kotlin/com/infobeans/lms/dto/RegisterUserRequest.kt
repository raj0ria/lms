package com.infobeans.lms.dto

data class RegisterUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String? = null
)
