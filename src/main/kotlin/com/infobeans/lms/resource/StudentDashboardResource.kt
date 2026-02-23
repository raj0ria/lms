package com.infobeans.lms.resource

import com.infobeans.lms.dto.ApiError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse

import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.StudentCourseDashboardResponse
import com.infobeans.lms.service.impl.StudentDashboardService
import org.slf4j.LoggerFactory
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Student Dashboard Controller.
 * Provides enrolled courses with progress details.
 */
@Tag(
    name = "Student Dashboard",
    description = "Endpoints for students to view enrolled courses and progress"
)
@RestController
@RequestMapping("/api/v1/students")
@SecurityRequirement(name = "bearerAuth")
class StudentDashboardResource(
    private val studentDashboardService: StudentDashboardService
) {

    private val log = LoggerFactory.getLogger(StudentDashboardResource::class.java)

    /**
     * Get paginated list of logged-in student's enrolled courses.
     */
    @Operation(
        summary = "Get my enrolled courses",
        description = "Returns paginated list of courses the logged-in STUDENT is enrolled in, including progress details."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Courses fetched successfully",
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
                description = "Access denied (Only STUDENT allowed)",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Unexpected server error",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/me/courses")
    fun getMyCourses(
        @ParameterObject
        @PageableDefault(
            size = 10,
            sort = ["enrolledAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable
    ): ResponseEntity<PagedResponse<StudentCourseDashboardResponse>> {

        log.info("GET /api/v1/students/me/courses invoked")
        val response = studentDashboardService.getMyCourses(pageable)

        return ResponseEntity.ok(response)
    }

}


