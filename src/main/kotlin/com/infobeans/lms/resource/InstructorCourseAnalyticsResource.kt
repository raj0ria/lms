package com.infobeans.lms.resource

import com.infobeans.lms.dto.ApiError
import com.infobeans.lms.dto.InstructorCourseStudentResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.service.impl.InstructorCourseAnalyticsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST resource exposing instructor analytics endpoints.
 */
@Tag(
    name = "Instructor Analytics",
    description = "Analytics endpoints for instructors to monitor enrolled students"
)
@RestController
@RequestMapping("/api/v1/instructors")
@SecurityRequirement(name = "bearerAuth")
class InstructorCourseAnalyticsResource(
    private val analyticsService: InstructorCourseAnalyticsService
) {

    private val log = LoggerFactory.getLogger(InstructorCourseAnalyticsResource::class.java)

    @Operation(
        summary = "Get enrolled students for course",
        description = "Returns paginated list of students enrolled in a course. Accessible only to INSTRUCTOR role."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Students fetched successfully",
                content = [Content(
                    schema = Schema(implementation = PagedResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "JWT expired or invalid",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Access denied (Only INSTRUCTOR allowed)",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Course not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Unexpected server error",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )

    /**
     * Returns paginated list of enrolled students for a course.
     */
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/courses/{courseId}/students")
    fun getCourseStudents(
        @Parameter(
            description = "ID of the course",
            example = "101",
            required = true
        )
        @PathVariable courseId: Long,
        @PageableDefault(
            size = 10,
            sort = ["enrolledAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable
    ): ResponseEntity<PagedResponse<InstructorCourseStudentResponse>> {

        log.info("GET /api/instructors/courses/{}/students invoked", courseId)

        val response = analyticsService.getCourseStudents(courseId, pageable)

        return ResponseEntity.ok(response)
    }
}
