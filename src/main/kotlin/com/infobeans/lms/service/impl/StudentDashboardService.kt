package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.StudentCourseDashboardResponse
import com.infobeans.lms.enums.Role
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.persistence.EnrollmentRepository
import com.infobeans.lms.persistence.UserRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service


@Service
class StudentDashboardService(
    private val enrollmentRepository: EnrollmentRepository,
    private val userRepository: UserRepository
) {

    private val log = LoggerFactory.getLogger(StudentDashboardService::class.java)

    @Transactional
    fun getMyCourses(pageable: Pageable): PagedResponse<StudentCourseDashboardResponse> {

        val email = SecurityContextHolder.getContext()
            .authentication.name

        log.info(
            "Dashboard request by user={} page={} size={} sort={}",
            email,
            pageable.pageNumber,
            pageable.pageSize,
            pageable.sort
        )

        val student = userRepository.findByEmail(email)
            ?: run {
                log.warn("Dashboard access failed - user not found: {}", email)
                throw ResourceNotFoundException("User not found")
            }

        if (student.role != Role.STUDENT) {
            log.warn(
                "Unauthorized dashboard access attempt by user={} role={}",
                email,
                student.role
            )
            throw AccessDeniedException("Only students can view this dashboard")
        }

        val page = enrollmentRepository.findStudentDashboard(student.id, pageable)

        val mappedContent = page.content.map {
            val progress =
                if (it.totalModules == 0L) 0
                else ((it.completedModules * 100) / it.totalModules).toInt()

            StudentCourseDashboardResponse(
                courseId = it.courseId,
                courseTitle = it.courseTitle,
                enrolledAt = it.enrolledAt,
                totalModules = it.totalModules,
                completedModules = it.completedModules,
                progressPercentage = progress
            )
        }

        return PagedResponse(
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            content = mappedContent
        )

    }
}


