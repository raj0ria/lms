package com.infobeans.lms.dto.projections

import java.time.Instant

interface CourseModuleProjection {
    val id: Long
    val name: String
    val materialUrl: String
    val createdAt: Instant
}
