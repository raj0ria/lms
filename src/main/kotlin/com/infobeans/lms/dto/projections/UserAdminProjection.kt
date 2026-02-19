package com.infobeans.lms.dto.projections

import com.infobeans.lms.enums.Role
import java.time.Instant

/**
 * Projection for admin user listing.
 */
interface UserAdminProjection {

    val id: Long
    val name: String
    val email: String
    val role: Role
    val createdAt: Instant
}
