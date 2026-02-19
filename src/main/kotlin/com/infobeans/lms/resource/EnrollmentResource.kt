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

@RestController
@RequestMapping("/api/v1/courses")
class EnrollmentResource(
    private val enrollmentService: EnrollmentService
) {

    private val log = LoggerFactory.getLogger(EnrollmentResource::class.java)

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{courseId}/enroll")
    fun enrollInCourse(
        @PathVariable courseId: Long
    ): ResponseEntity<EnrollmentResponse> {

        log.info("Received enrollment request for course {}", courseId)

        val response = enrollmentService.enrollInCourse(courseId)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

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
