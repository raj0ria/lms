package com.infobeans.lms.dto

import java.time.Instant

/**
 * Admin course listing response.
 */
data class AdminCourseResponse(
    val id: Long,
    val title: String,
    val description: String,
    val published: Boolean,
    val capacity: Int,
    val instructorName: String,
    val createdAt: Instant
)

