package com.infobeans.lms.dto

import java.time.Instant

data class StudentCourseDashboardResponse(
    val courseId: Long,
    val courseTitle: String,
    val enrolledAt: Instant,
    val totalModules: Long,
    val completedModules: Long,
    val progressPercentage: Int
)

