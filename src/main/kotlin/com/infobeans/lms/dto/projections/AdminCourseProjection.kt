package com.infobeans.lms.dto.projections

import java.time.Instant

/**
 * Projection for admin course listing.
 */
interface AdminCourseProjection {

    val id: Long
    val title: String
    val description: String
    val published: Boolean
    val capacity: Int
    val instructorName: String
    val createdAt: Instant
}
