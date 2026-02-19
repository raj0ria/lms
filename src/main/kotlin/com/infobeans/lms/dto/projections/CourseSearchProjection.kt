package com.infobeans.lms.dto.projections

import java.time.Instant

/**
 * Projection for course search listing.
 * Avoids loading full entity graph.
 */
interface CourseSearchProjection {

    val courseId: Long
    val title: String
    val description: String
    val instructorName: String
    val capacity: Int
    val enrolledCount: Long
    val createdAt: Instant
}
