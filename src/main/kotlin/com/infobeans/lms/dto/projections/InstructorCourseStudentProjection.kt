package com.infobeans.lms.dto.projections

import java.time.Instant

/**
 * Projection for instructor course student dashboard.
 * Aggregates module progress per student enrollment.
 */
interface InstructorCourseStudentProjection {

    val studentId: Long
    val studentName: String
    val studentEmail: String
    val enrolledAt: Instant
    val totalModules: Long
    val completedModules: Long
}