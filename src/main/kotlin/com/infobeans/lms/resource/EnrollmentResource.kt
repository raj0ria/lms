package com.infobeans.lms.resource

import com.infobeans.lms.dto.EnrollmentResponse
import com.infobeans.lms.service.EnrollmentService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
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
@RestController
@RequestMapping("/api/v1/courses")
class EnrollmentResource(
    private val enrollmentService: EnrollmentService
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
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{courseId}/enroll")
    fun enrollInCourse(
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
    @PreAuthorize("hasRole('STUDENT')")
    @DeleteMapping("/{courseId}/unenroll")
    fun unenrollFromCourse(
        @PathVariable courseId: Long
    ): ResponseEntity<Void> {

        log.info("Received unenrollment request for course {}", courseId)

        enrollmentService.unenrollFromCourse(courseId)

        return ResponseEntity.noContent().build()
    }

}
