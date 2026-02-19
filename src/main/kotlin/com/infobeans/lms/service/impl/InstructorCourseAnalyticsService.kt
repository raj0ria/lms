package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.InstructorCourseStudentResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.enums.Role
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.EnrollmentRepository
import com.infobeans.lms.persistence.UserRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

/**
 * Service responsible for instructor-facing analytics operations.
 */
@Service
class InstructorCourseAnalyticsService(
    private val enrollmentRepository: EnrollmentRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository
) {

    private val log = LoggerFactory.getLogger(InstructorCourseAnalyticsService::class.java)

    /**
     * Retrieves paginated list of enrolled students with progress summary
     * for a specific course owned by the authenticated instructor.
     *
     * @param courseId ID of the course
     * @param pageable pagination and sorting information
     * @return paginated response of student progress data
     */
    @Transactional()
    fun getCourseStudents(
        courseId: Long,
        pageable: Pageable
    ): PagedResponse<InstructorCourseStudentResponse> {

        val email = SecurityContextHolder.getContext()
            .authentication.name

        log.info(
            "Instructor analytics request for courseId={} by user={}",
            courseId,
            email
        )

        val instructor = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found")

        if (instructor.role != Role.INSTRUCTOR) {
            log.warn("Unauthorized analytics access attempt by user={}", email)
            throw AccessDeniedException("Only instructors can access this resource")
        }

        val course = courseRepository.findById(courseId)
            .orElseThrow { ResourceNotFoundException("Course not found") }

        if (course.instructor.id != instructor.id) {
            log.warn(
                "Instructor {} attempted access to foreign course {}",
                email,
                courseId
            )
            throw AccessDeniedException("You can only access your own course analytics")
        }

        val page = enrollmentRepository.findInstructorCourseStudents(courseId, pageable)

        val content = page.content.map {
            val progress =
                if (it.totalModules == 0L) 0
                else ((it.completedModules * 100) / it.totalModules).toInt()

            InstructorCourseStudentResponse(
                studentId = it.studentId,
                studentName = it.studentName,
                studentEmail = it.studentEmail,
                enrolledAt = it.enrolledAt,
                totalModules = it.totalModules,
                completedModules = it.completedModules,
                progressPercentage = progress
            )
        }

        log.info(
            "Instructor analytics response prepared courseId={} totalStudents={}",
            courseId,
            page.totalElements
        )

        return PagedResponse(
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            content = content
        )
    }
}
