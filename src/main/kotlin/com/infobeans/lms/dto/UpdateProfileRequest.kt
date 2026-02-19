package com.infobeans.lms.dto

import jakarta.validation.constraints.NotBlank

/**
 * Request DTO for updating user profile.
 */
data class UpdateProfileRequest(
    @field:NotBlank(message = "Name is required")
    val name: String
)

