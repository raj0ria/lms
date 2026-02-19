package com.infobeans.lms.dto

import com.infobeans.lms.enums.Role

/**
 * Request DTO for creating a user.
 */
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: Role
)
