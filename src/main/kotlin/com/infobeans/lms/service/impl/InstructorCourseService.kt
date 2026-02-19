package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.CreateCourseRequest
import com.infobeans.lms.dto.CourseResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.UpdateCourseRequest
import com.infobeans.lms.enums.Role
import com.infobeans.lms.exceptions.BusinessRuleViolationException
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.model.Course
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class InstructorCourseService(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository
) {

    private val log = LoggerFactory.getLogger(InstructorCourseService::class.java)

    @Transactional
    fun createCourse(request: CreateCourseRequest): CourseResponse {

        val email = SecurityContextHolder.getContext()
            .authentication.name

        log.info("Attempting to create course with title: {} by instructor: {}", request.title, email)

        val instructor = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("Instructor not found")

        if (instructor.role != Role.INSTRUCTOR) {
            log.warn("Unauthorized course creation attempt by user: {}", email)
            throw AccessDeniedException("Only instructors can create courses")
        }

        if (courseRepository.existsByTitle(request.title)) {
            log.warn("Duplicate course title attempted: {}", request.title)
            throw BusinessRuleViolationException("Course title already exists")
        }

        val course = Course(
            title = request.title,
            description = request.description,
            capacity = request.capacity,
            published = false
        )

        course.instructor = instructor

        val savedCourse = courseRepository.save(course)

        log.info("Course created successfully with id: {} and title: {}", savedCourse.id, savedCourse.title)

        return savedCourse.toResponse()
    }

    @Transactional
    fun publishCourse(courseId: Long): CourseResponse {

        val email = SecurityContextHolder.getContext()
            .authentication.name

        log.info("Instructor {} attempting to publish course {}", email, courseId)

        val course = courseRepository.findById(courseId)
            .orElseThrow { ResourceNotFoundException("Course not found") }

        if (course.instructor.email != email) {
            log.warn("Unauthorized publish attempt by {}", email)
            throw AccessDeniedException("You can only publish your own course")
        }

        if (course.modules.isEmpty()) {
            throw BusinessRuleViolationException(
                "Cannot publish course without modules"
            )
        }

        if (course.published) {
            throw BusinessRuleViolationException(
                "Course is already published"
            )
        }

        course.published = true

        log.info("Course {} successfully published", courseId)

        return course.toResponse()
    }

    /**
     * Returns paginated list of courses owned by instructor.
     */
    @Transactional(readOnly = true)
    fun getInstructorCourses(pageable: Pageable): PagedResponse<CourseResponse> {

        val email = SecurityContextHolder.getContext().authentication.name

        val instructor = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("Instructor not found")

        log.info("Instructor {} fetching own courses", email)

        val page = courseRepository.findByInstructorId(instructor.id, pageable)

        val content = page.content.map {
            CourseResponse(
                id = it.id,
                title = it.title,
                description = it.description,
                published = it.published,
                capacity = it.capacity,
                instructorName = instructor.name,
                instructorEmail = instructor.email,
                createdAt = it.createdAt,
                updatedAt = Instant.now()
            )
        }

        return PagedResponse(
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            content = content
        )
    }

    /**
     * Updates course owned by instructor.
     */
    @Transactional
    fun updateCourse(courseId: Long, request: UpdateCourseRequest): CourseResponse {

        val email = SecurityContextHolder.getContext().authentication.name

        log.info("Instructor {} attempting to update course {}", email, courseId)

        val course = courseRepository.findById(courseId)
            .orElseThrow { ResourceNotFoundException("Course not found") }

        if (course.instructor.email != email) {
            log.warn("Unauthorized update attempt by {}", email)
            throw AccessDeniedException("You can only update your own course")
        }

        request.title.let {
            if (courseRepository.existsByTitle(it) && course.title != it) {
                throw BusinessRuleViolationException("Course title already exists")
            }
            course.title = it
        }

        request.description?.let { course.description = it }
        request.capacity?.let { course.capacity = it }

        log.info("Course {} updated successfully", courseId)

        return course.toResponse()
    }

    /**
     * Deletes course owned by instructor.
     */
    @Transactional
    fun deleteCourse(courseId: Long) {

        val email = SecurityContextHolder.getContext().authentication.name

        log.info("Instructor {} attempting to delete course {}", email, courseId)

        val course = courseRepository.findById(courseId)
            .orElseThrow { ResourceNotFoundException("Course not found") }

        if (course.instructor.email != email) {
            log.warn("Unauthorized delete attempt by {}", email)
            throw AccessDeniedException("You can only delete your own course")
        }

        courseRepository.delete(course)

        log.info("Course {} deleted successfully by instructor {}", courseId, email)
    }


    private fun Course.toResponse(): CourseResponse {
        return CourseResponse(
            id = this.id,
            title = this.title,
            description = this.description,
            published = this.published,
            capacity = this.capacity,
            instructorName = this.instructor.name,
            instructorEmail = this.instructor.email,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
