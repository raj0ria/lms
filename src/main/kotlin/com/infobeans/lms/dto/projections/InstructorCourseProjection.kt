package com.infobeans.lms.dto.projections

import java.time.Instant

/**
 * Projection for instructor course listing.
 */
interface InstructorCourseProjection {
    val id: Long
    val title: String
    val description: String
    val published: Boolean
    val capacity: Int
    val createdAt: Instant
}
