package com.infobeans.lms.dto

import java.time.Instant

data class CourseResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val published: Boolean,
    val capacity: Int,
    val instructorName: String,
    val instructorEmail: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
