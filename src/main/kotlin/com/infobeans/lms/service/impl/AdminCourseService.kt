package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.AdminCourseResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.persistence.CourseRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service responsible for administrative course management.
 */
@Service
class AdminCourseService(
    private val courseRepository: CourseRepository,
) {

    private val log = LoggerFactory.getLogger(AdminCourseService::class.java)

    /**
     * Returns paginated list of all courses including unpublished ones.
     */
    @Transactional(readOnly = true)
    fun getAllCourses(
        keyword: String?,
        pageable: Pageable
    ): PagedResponse<AdminCourseResponse> {

        log.info(
            "Admin fetching all courses keyword={} page={} size={}",
            keyword,
            pageable.pageNumber,
            pageable.pageSize
        )

        val page = courseRepository.findAllAdminCourses(keyword, pageable)

        val content = page.content.map {
            AdminCourseResponse(
                id = it.id,
                title = it.title,
                description = it.description,
                published = it.published,
                capacity = it.capacity,
                instructorName = it.instructorName,
                createdAt = it.createdAt
            )
        }

        log.info(
            "Admin course listing returned {} results (total={})",
            page.numberOfElements,
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

    /**
     * Deletes a course by ID.
     */
    @Transactional
    fun deleteCourse(courseId: Long) {

        log.info("Admin attempting to delete course id={}", courseId)

        val course = courseRepository.findById(courseId)
            .orElseThrow { ResourceNotFoundException("Course not found") }

        courseRepository.delete(course)

        log.info("Course id={} deleted successfully by admin", courseId)
    }
}
