package com.infobeans.lms.dto

import java.time.Instant

/**
 * Response DTO representing student progress in instructor dashboard.
 */
data class InstructorCourseStudentResponse(
    val studentId: Long,
    val studentName: String,
    val studentEmail: String,
    val enrolledAt: Instant,
    val totalModules: Long,
    val completedModules: Long,
    val progressPercentage: Int
)
