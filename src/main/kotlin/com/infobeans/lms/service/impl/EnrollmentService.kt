package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.EnrollmentResponse
import com.infobeans.lms.enums.EnrollmentStatus
import com.infobeans.lms.enums.Role
import com.infobeans.lms.exceptions.BusinessRuleViolationException
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.model.Enrollment
import com.infobeans.lms.model.StudentEnrollmentStatus
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.EnrollmentRepository
import com.infobeans.lms.persistence.UserRepository
import io.micrometer.core.instrument.MeterRegistry
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

/**
 * Service responsible for enrollment business logic.
 *
 * Responsibilities:
 * - Enroll student in course
 * - Validate business constraints
 * - Enforce RBAC at business layer
 * - Initialize module progress tracking
 * - Support observability via logging & metrics
 *
 * Layer: Service (Business Logic)
 */

@Service
class EnrollmentService(
    private val enrollmentRepository: EnrollmentRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val meterRegistry: MeterRegistry
) {

    private val log = LoggerFactory.getLogger(EnrollmentService::class.java)

    /**
     * Micrometer counter to track enrollment events.
     * Exposed via Act  uator /metrics endpoint.
     */
    private val enrollmentCount = meterRegistry.counter("enrollment_count")

    /**
     * Enrolls authenticated student into a course.
     *
     * Business Rules:
     * - User must exist
     * - User must have STUDENT role
     * - Course must exist
     * - Course must be published
     * - Duplicate enrollment must not occur
     * - Course capacity must not be exceeded
     *
     * Also:
     * - Initializes module completion tracking
     * - Increments enrollment metric counter
     *
     * @param courseId ID of the course
     * @return EnrollmentResponse
     */
    @Transactional
    fun enrollInCourse(courseId: Long): EnrollmentResponse {

        // Increment enrollment metric for observability
        enrollmentCount.increment()

        // Extract authenticated user's email from security context
        val email = SecurityContextHolder.getContext()
            .authentication.name

        log.info("User {} attempting to enroll in course {}", email, courseId)

        // Validate student existence
        val student = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found")

        // RBAC enforcement at service layer
        if (student.role != Role.STUDENT) {
            throw AccessDeniedException("Only students are allowed to enroll")
        }

        // Validate course existence
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResourceNotFoundException("Course not found") }

        // Ensure course is published before enrollment
        if (!course.published) {
            throw BusinessRuleViolationException("Course is not published")
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(student.id, courseId)) {
            throw BusinessRuleViolationException("You are already enrolled in this course")
        }

        val currentCount = enrollmentRepository.countByCourseId(courseId)
        if (currentCount >= course.capacity) {
            throw BusinessRuleViolationException("Course capacity has been reached")
        }

        val enrollment = Enrollment()
        enrollment.user = student
        enrollment.course = course

        /**
         * Initialize module progress tracking.
         * Each module starts with NOT_STARTED status.
         */
        course.modules.forEach { module ->
            val status = StudentEnrollmentStatus(
                status = EnrollmentStatus.NOT_STARTED
            )
            status.module = module
            status.enrollment = enrollment
            enrollment.moduleStatuses.add(status)
        }

        try {
            val savedEnrollment = enrollmentRepository.save(enrollment)

            log.info("User {} successfully enrolled in course {}", email, courseId)

            return savedEnrollment.toResponse()

        } catch (ex: DataIntegrityViolationException) {
            throw BusinessRuleViolationException("You are already enrolled in this course")
        }
    }

    /**
     * Allows student to unenroll from a course.
     */
    @Transactional
    fun unenrollFromCourse(courseId: Long) {

        val email = SecurityContextHolder.getContext()
            .authentication.name

        log.info("User {} attempting to unenroll from course {}", email, courseId)

        val student = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found")

        if (student.role != Role.STUDENT) {
            throw AccessDeniedException("Only students can unenroll")
        }

        val enrollment = enrollmentRepository
            .findByUserIdAndCourseId(student.id, courseId)
            ?: throw BusinessRuleViolationException("You are not enrolled in this course")

        enrollmentRepository.delete(enrollment)

        log.info(
            "User {} successfully unenrolled from course {}",
            email,
            courseId
        )
    }

    /**
     * Maps Enrollment entity to EnrollmentResponse DTO.
     */
    private fun Enrollment.toResponse(): EnrollmentResponse {
        return EnrollmentResponse(
            id = this.id,
            courseId = this.course.id,
            courseTitle = this.course.title,
            enrolledAt = this.enrolledAt
        )
    }
}
