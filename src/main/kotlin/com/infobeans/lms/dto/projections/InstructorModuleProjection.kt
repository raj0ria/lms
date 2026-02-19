package com.infobeans.lms.dto.projections

import java.time.Instant

/**
 * Projection for instructor module listing.
 */
interface InstructorModuleProjection {
    val id: Long
    val name: String
    val materialUrl: String
    val createdAt: Instant
}
