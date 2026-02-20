package com.infobeans.lms.dto

/**
 * Response DTO for admin dashboard summary.
 */
data class AdminDashboardSummaryResponse(
    val totalStudents: Long,
    val totalInstructors: Long,
    val totalCourses: Long,
    val totalEnrollments: Long
)

