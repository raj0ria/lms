package com.infobeans.lms.dto.projections

import java.time.Instant

/**
 * Projection for course detail (basic information).
 */
interface CourseDetailProjection {

    val id: Long
    val title: String
    val description: String
    val capacity: Int
    val published: Boolean
    val instructorId: Long
    val instructorName: String
    val instructorEmail: String
    val createdAt: Instant
    val updatedAt: Instant
}
