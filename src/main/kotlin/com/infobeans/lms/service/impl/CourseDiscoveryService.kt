package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.*
import com.infobeans.lms.enums.Role
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.EnrollmentRepository
import com.infobeans.lms.persistence.ModuleRepository
import com.infobeans.lms.persistence.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Service responsible for course discovery operations.
 */
@Service
class CourseDiscoveryService(
    private val courseRepository: CourseRepository,
    private val moduleRepository: ModuleRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val userRepository: UserRepository
) {

    private val log = LoggerFactory.getLogger(CourseDiscoveryService::class.java)

    /**
     * Searches published courses with optional keyword filtering.
     *
     * @param keyword optional title search keyword
     * @param pageable pagination and sorting parameters
     * @return paginated list of courses
     */
    @Transactional(readOnly = true)
    fun searchCourses(
        keyword: String?,
        pageable: Pageable
    ): PagedResponse<CourseSearchResponse> {

        log.info(
            "Course search request keyword={} page={} size={} sort={}",
            keyword,
            pageable.pageNumber,
            pageable.pageSize,
            pageable.sort
        )

        val page = courseRepository.searchPublishedCourses(keyword, pageable)

        val content = page.content.map {
            CourseSearchResponse(
                courseId = it.courseId,
                title = it.title,
                description = it.description,
                instructorName = it.instructorName,
                capacity = it.capacity,
                enrolledCount = it.enrolledCount,
                createdAt = it.createdAt
            )
        }

        log.info(
            "Course search completed. totalResults={} returned={}",
            page.totalElements,
            page.numberOfElements
        )

        return PagedResponse(
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            content = content
        )
    }

    /**
     * Returns detailed course information including modules and enrolled users.
     * ADMIN can view any course.
     * INSTRUCTOR can only view their own course.
     */
    @Transactional(readOnly = true)
    fun getCourseDetail(courseId: Long): CourseDetailResponse {

        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name

        log.info(
            "Course detail request for courseId={} by user={}",
            courseId,
            email
        )

        val user = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found")

        val course = courseRepository.findCourseDetail(courseId)
            ?: throw ResourceNotFoundException("Course not found")

        // 🔐 Role-based ownership validation
        if (user.role == Role.INSTRUCTOR) {

            if (course.instructorEmail != user.email) {
                log.warn(
                    "Instructor {} attempted to access course {} not owned by them",
                    email,
                    courseId
                )
                throw AccessDeniedException("You can only view your own course details")
            }
        }

        // ADMIN automatically allowed

        val modules = moduleRepository.findModulesByCourseId(courseId)

        val enrolledUsers =
            enrollmentRepository.findEnrolledUsersByCourseId(courseId)

        log.info(
            "Course detail fetched successfully id={} modules={} enrollments={}",
            courseId,
            modules.size,
            enrolledUsers.size
        )

        return CourseDetailResponse(
            id = course.id,
            title = course.title,
            description = course.description,
            capacity = course.capacity,
            published = course.published,
            instructor = InstructorInfo(
                id = course.instructorId,
                name = course.instructorName,
                email = course.instructorEmail
            ),
            modules = modules.map {
                ModuleInfo(
                    id = it.id,
                    name = it.name,
                    materialUrl = it.materialUrl,
                    createdAt = it.createdAt
                )
            },
            enrollments = enrolledUsers.map {
                EnrollmentUserInfo(
                    userId = it.userId,
                    name = it.name,
                    email = it.email,
                    role = it.role,
                    enrolledAt = it.enrolledAt
                )
            },
            createdAt = course.createdAt,
            updatedAt = course.updatedAt
        )
    }


    @Transactional(readOnly = true)
    fun getModulesByCourse(courseId: Long): List<ModuleResponse> {
        return moduleRepository.findByCourseId(courseId).map { module ->
            ModuleResponse(
                id = module.id,
                name = module.name,
                materialUrl = module.materialUrl,
                courseId = module.course.id,      // reference to parent course
                createdAt = module.createdAt ?: Instant.now(),
                updatedAt = module.updatedAt ?: Instant.now()
            )
        }
    }

}
