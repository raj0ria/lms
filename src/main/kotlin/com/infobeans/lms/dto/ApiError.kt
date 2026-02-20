package com.infobeans.lms.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Standard API error response")
data class ApiError(
    @Schema(description = "Error timestamp", example = "2026-02-20T11:08:32.462476700Z")
    val timestamp: Instant = Instant.now(),

    @Schema(description = "HTTP status code", example = "404")
    val status: Int,

    @Schema(description = "Error reason phrase", example = "Not Found")
    val error: String,

    @Schema(description = "Detailed error message", example = "Course not found")
    val message: String?,

    @Schema(description = "Request path where error occurred", example = "/api/v1/courses/10/enroll")
    val path: String
)
