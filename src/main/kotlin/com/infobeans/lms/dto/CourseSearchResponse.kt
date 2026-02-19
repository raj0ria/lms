package com.infobeans.lms.dto

import java.time.Instant

/**
 * Response DTO for course listing.
 */
data class CourseSearchResponse(
    val courseId: Long,
    val title: String,
    val description: String,
    val instructorName: String,
    val capacity: Int,
    val enrolledCount: Long,
    val createdAt: Instant
)
