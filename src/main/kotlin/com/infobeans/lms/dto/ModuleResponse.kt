package com.infobeans.lms.dto

import java.time.Instant

data class ModuleResponse(
    val id: Long,
    val name: String,
    val materialUrl: String,
    val courseId: Long,
    val createdAt: Instant,
    val updatedAt: Instant
)