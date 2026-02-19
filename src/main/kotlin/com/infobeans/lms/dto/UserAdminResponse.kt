package com.infobeans.lms.dto

import com.infobeans.lms.enums.Role
import java.time.Instant

/**
 * Admin user response DTO.
 */
data class UserAdminResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: Role,
    val createdAt: Instant
)

