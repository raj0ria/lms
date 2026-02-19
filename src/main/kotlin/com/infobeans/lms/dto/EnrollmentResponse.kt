package com.infobeans.lms.dto

import java.time.Instant

data class EnrollmentResponse(
    val id: Long,
    val courseId: Long,
    val courseTitle: String,
    val enrolledAt: Instant
)
