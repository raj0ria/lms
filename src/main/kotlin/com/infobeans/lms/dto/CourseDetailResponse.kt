package com.infobeans.lms.dto

import com.infobeans.lms.enums.Role
import java.time.Instant

data class CourseDetailResponse(
    val id: Long,
    val title: String,
    val description: String,
    val capacity: Int,
    val published: Boolean,
    val instructor: InstructorInfo,
    val modules: List<ModuleInfo>,
    val enrollments: List<EnrollmentUserInfo>,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class InstructorInfo(
    val id: Long,
    val name: String,
    val email: String
)

data class ModuleInfo(
    val id: Long,
    val name: String,
    val materialUrl: String,
    val createdAt: Instant
)

data class EnrollmentUserInfo(
    val userId: Long,
    val name: String,
    val email: String,
    val role: Role,
    val enrolledAt: Instant
)

