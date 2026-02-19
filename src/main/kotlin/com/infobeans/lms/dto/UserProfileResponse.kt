package com.infobeans.lms.dto

import com.infobeans.lms.enums.Role
import java.time.Instant

/**
 * Response DTO representing authenticated user's profile.
 */
data class UserProfileResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: Role,
    val createdAt: Instant,
    val updatedAt: Instant
)

