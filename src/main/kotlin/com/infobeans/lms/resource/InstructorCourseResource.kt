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

import com.infobeans.lms.dto.CreateCourseRequest
import com.infobeans.lms.dto.CourseResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.UpdateCourseRequest
import com.infobeans.lms.service.impl.InstructorCourseService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Instructor Course Controller.
 * Handles course CRUD operations for authenticated instructors.
 */
@Tag(
    name = "Instructor Course Management",
    description = "CRUD operations for instructors managing their courses"
)
@RestController
@RequestMapping("/api/v1/instructor/courses")
@PreAuthorize("hasRole('INSTRUCTOR')")
@SecurityRequirement(name = "bearerAuth")
class InstructorCourseResource(
    private val courseService: InstructorCourseService
) {

    private val log = LoggerFactory.getLogger(InstructorCourseResource::class.java)

    /**
     * Create a new course.
     */
    @Operation(summary = "Create new course")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Course created successfully",
                content = [Content(schema = Schema(implementation = CourseResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Validation or business rule violation",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "JWT expired or invalid",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Access denied",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCourse(
        @Valid @RequestBody request: CreateCourseRequest
    ): CourseResponse {
        log.info("POST /api/v1/instructor/courses invoked")
        return courseService.createCourse(request)
    }

    /**
     * Update existing course.
     */
    @Operation(summary = "Update existing course")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Course updated successfully",
                content = [Content(schema = Schema(implementation = CourseResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Course not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid update request",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PutMapping("/{courseId}")
    fun updateCourse(
        @PathVariable courseId: Long,
        @RequestBody request: UpdateCourseRequest
    ): CourseResponse {

        log.info("PUT /api/v1/instructor/courses/{}", courseId)

        return courseService.updateCourse(courseId, request)
    }

    /**
     * Delete    a course.
     */
    @Operation(summary = "Delete course")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Course deleted successfully"),
            ApiResponse(
                responseCode = "404",
                description = "Course not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @DeleteMapping("/{courseId}")
    fun deleteCourse(
        @PathVariable courseId: Long
    ): ResponseEntity<Void> {

        log.info("DELETE /api/v1/instructor/courses/{}", courseId)

        courseService.deleteCourse(courseId)

        return ResponseEntity.noContent().build()
    }

    /**
     * Publish a course (make visible to students).
     */
    @Operation(summary = "Publish course")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Course published successfully",
                content = [Content(schema = Schema(implementation = CourseResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Course not found",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Course cannot be published (business rule violation)",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @PatchMapping("/{courseId}/publish")
    fun publishCourse(
        @PathVariable courseId: Long
    ): CourseResponse {

        log.info("PATCH /api/v1/instructor/courses/{}/publish", courseId)

        return courseService.publishCourse(courseId)
    }

    /**
     * Get paginated list of instructor's own courses.
     */
    @Operation(summary = "Get instructor's own courses")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Courses fetched successfully",
                content = [Content(schema = Schema(implementation = PagedResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "JWT expired or invalid",
                content = [Content(schema = Schema(implementation = ApiError::class))]
            )
        ]
    )
    @GetMapping
    fun getOwnCourses(
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<PagedResponse<CourseResponse>> {

        log.info("GET /api/v1/instructor/courses invoked")

        return ResponseEntity.ok(courseService.getInstructorCourses(pageable))
    }
}

