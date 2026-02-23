package com.infobeans.lms.resource

import com.infobeans.lms.dto.ApiError
import com.infobeans.lms.dto.EnrollmentResponse
import com.infobeans.lms.dto.StudentModuleProgressResponse
import com.infobeans.lms.persistence.UserRepository
import com.infobeans.lms.service.impl.EnrollmentService
import com.infobeans.lms.service.impl.ModuleProgressService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST Controller responsible for student enrollment operations.
 *
 * Base Path: /api/v1/courses
 *
 * Responsibilities:
 * - Enroll student into a course
 * - Unenroll student from a course
 *
 * Security:
 * - Only STUDENT role allowed (RBAC enforced via @PreAuthorize)
 *
 * Observability:
 * - Structured logging for enrollment actions
 */
@Tag(
    name = "Enrollment",
    description = "Student enrollment operations for courses"
)
@RestController
@RequestMapping("/api/v1/courses")
@PreAuthorize("hasRole('STUDENT')")
@SecurityRequirement(name = "bearerAuth")
class EnrollmentResource(
    private val enrollmentService: EnrollmentService,
    private val userRepository: UserRepository,
    private val moduleProgressService: ModuleProgressService
) {

    private val log = LoggerFactory.getLogger(EnrollmentResource::class.java)

    /**
     * Enrolls the authenticated student into a course.
     *
     * Business Rules enforced in service layer:
     * - Course must exist
     * - Course must be published
     * - Student must not already be enrolled
     * - Course capacity must not be exceeded
     *
     * @param courseId ID of the course to enroll into
     * @return EnrollmentResponse containing enrollment details
     */
    @Operation(
        summary = "Enroll into course",
        description = "Enrolls authenticated STUDENT into a published course. Capacity and duplicate enrollment rules are enforced."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Enrollment successful", content = [Content(
                    schema = Schema(implementation = EnrollmentResponse::class))]),
            ApiResponse(responseCode = "400", description = "Business rule violation",
                content = [Content(
                    schema = Schema(implementation = ApiError::class))]),
            ApiResponse(responseCode = "401", description = "Business rule violation",
                content = [Content(
                    schema = Schema(implementation = ApiError::class))]),
            ApiResponse(
                responseCode = "401",
                description = "JWT expired or invalid",
                content = [Content(
                    schema = Schema(implementation = ApiError::class)
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Access denied",
                content = [Content(
                    schema = Schema(implementation = ApiError::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Course not found",
                content = [Content(
                    schema = Schema(implementation = ApiError::class)
                )]
            ),
            ApiResponse(
                responseCode = "409",
                description = "Already enrolled",
                content = [Content(
                    schema = Schema(implementation = ApiError::class)
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Unexpected server error",
                content = [Content(
                    schema = Schema(implementation = ApiError::class)
                )]
            )
        ]
    )
    @PostMapping("/{courseId}/enroll")
    fun enrollInCourse(
        @Parameter(
            description = "ID of the course to enroll into",
            example = "101",
            required = true,
            `in` = ParameterIn.PATH
        )
        @PathVariable courseId: Long
    ): ResponseEntity<EnrollmentResponse> {

        log.info("Received enrollment request for course {}", courseId)

        val response = enrollmentService.enrollInCourse(courseId)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Allows a student to unenroll from a course.
     *
     * Business Rules:
     * - Student must already be enrolled
     *
     * @param courseId ID of the course
     * @return 204 No Content if successful
     */
    @Operation(
        summary = "Unenroll from course",
        description = "Allows authenticated STUDENT to unenroll from a course they are currently enrolled in."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Unenrolled successfully"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Access denied (Only STUDENT allowed)"),
            ApiResponse(responseCode = "404", description = "Enrollment not found")
        ]
    )
    @DeleteMapping("/{courseId}/unenroll")
    fun unenrollFromCourse(
        @Parameter(
            description = "ID of the course to unenroll from",
            example = "101",
            required = true,
            `in` = ParameterIn.PATH
        )
        @PathVariable courseId: Long
    ): ResponseEntity<Void> {

        log.info("Received unenrollment request for course {}", courseId)

        enrollmentService.unenrollFromCourse(courseId)

        return ResponseEntity.noContent().build()
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/{courseId}/modules/progress")
    fun getModulesWithProgress(
        @PathVariable courseId: Long,
        authentication: Authentication
    ): ResponseEntity<List<StudentModuleProgressResponse>> {

        val username = authentication.name

        val user = userRepository.findByEmail(username)
            ?: throw RuntimeException("User not found")

        val response = moduleProgressService
            .getModulesWithProgress(courseId, user.id)

        return ResponseEntity.ok(response)
    }
}
