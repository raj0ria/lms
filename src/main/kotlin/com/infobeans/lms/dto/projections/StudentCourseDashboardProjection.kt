package com.infobeans.lms.dto.projections

import java.time.Instant

interface StudentCourseDashboardProjection {

    val courseId: Long
    val courseTitle: String
    val enrolledAt: Instant
    val totalModules: Long
    val completedModules: Long
}