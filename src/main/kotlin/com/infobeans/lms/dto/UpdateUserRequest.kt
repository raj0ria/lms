package com.infobeans.lms.dto

import com.infobeans.lms.enums.Role

/**
 * Request DTO for updating a user.
 */
data class UpdateUserRequest(
    val name: String?,
    val role: Role?
)