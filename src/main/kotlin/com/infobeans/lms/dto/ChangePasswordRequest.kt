package com.infobeans.lms.dto

import jakarta.validation.constraints.NotBlank

/**
 * Request DTO for changing password.
 */
data class ChangePasswordRequest(

    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    val newPassword: String
)

