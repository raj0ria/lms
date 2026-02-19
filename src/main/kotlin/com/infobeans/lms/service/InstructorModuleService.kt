package com.infobeans.lms.service

import com.infobeans.lms.dto.CreateModuleRequest
import com.infobeans.lms.dto.ModuleResponse
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.UpdateModuleRequest
import com.infobeans.lms.exceptions.DuplicateModuleException
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.model.Module
import com.infobeans.lms.persistence.CourseRepository
import com.infobeans.lms.persistence.ModuleRepository
import com.infobeans.lms.persistence.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InstructorModuleService(
    private val courseRepository: CourseRepository,
    private val moduleRepository: ModuleRepository,
    private val userRepository: UserRepository
) {

    private val log = LoggerFactory.getLogger(InstructorModuleService::class.java)

    /**
     * Role: Instructor
     * Create a course module using course id and module data
     */
    @Transactional
    fun createModule(courseId: Long, request: CreateModuleRequest): ModuleResponse {

        val email = SecurityContextHolder.getContext()
            .authentication.name

        log.info("Instructor {} attempting to add module to course {}", email, courseId)

        val course = courseRepository.findById(courseId)
            .orElseThrow { ResourceNotFoundException("Course not found") }

        val user = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found")

        if (course.instructor.email != email) {
            log.warn("Unauthorized module creation attempt by {}", email)
            throw AccessDeniedException("You can only modify your own course")
        }

        val normalizedName = request.name.trim().lowercase() //converted to lowercase before saving

        //added this
        if (moduleRepository.existsByCourseIdAndName(course.id, normalizedName)) {
            throw DuplicateModuleException("You can't have duplicate modules in a same")
        }

        val module = Module(
            name = normalizedName,
            materialUrl = request.materialUrl
        )

        module.course = course

        try {
            val saved = moduleRepository.save(module)
            log.info("Module {} created successfully for course {}", saved.id, courseId)
            return saved.toResponse()
        } catch (ex: DataIntegrityViolationException) {
            throw DuplicateModuleException("Module with this name already exists for the course")
        }

    }

    /**
     * Returns paginated modules for instructor's course.
     */
    @Transactional(readOnly = true)
    fun getModulesByCourse(
        courseId: Long,
        pageable: Pageable
    ): PagedResponse<ModuleResponse> {

        val email = SecurityContextHolder.getContext().authentication.name

        log.info("Instructor {} fetching modules for course {}", email, courseId)

        val course = courseRepository.findById(courseId)
            .orElseThrow { ResourceNotFoundException("Course not found") }

        if (course.instructor.email != email) {
            throw AccessDeniedException("You can only access your own course modules")
        }

        val page = moduleRepository.findByCourseId(courseId, pageable)

        val content = page.content.map {
            ModuleResponse(
                id = it.id,
                name = it.name,
                materialUrl = it.materialUrl,
                courseId = courseId,
                createdAt = it.createdAt,
                updatedAt = it.createdAt
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
     * Updates module owned by instructor.
     */
    @Transactional
    fun updateModule(
        moduleId: Long,
        request: UpdateModuleRequest
    ): ModuleResponse {

        val email = SecurityContextHolder.getContext().authentication.name

        log.info("Instructor {} attempting to update module {}", email, moduleId)

        val module = moduleRepository.findById(moduleId)
            .orElseThrow { ResourceNotFoundException("Module not found") }

        if (module.course.instructor.email != email) {
            log.warn("Unauthorized module update attempt by {}", email)
            throw AccessDeniedException("You can only update your own module")
        }

        request.name?.let {
            val normalized = it.trim().lowercase()
            if (moduleRepository.existsByCourseIdAndName(module.course.id, normalized)
                && module.name != normalized
            ) {
                throw DuplicateModuleException("Module name already exists in this course")
            }
            module.name = normalized
        }

        request.materialUrl?.let { module.materialUrl = it }

        log.info("Module {} updated successfully", moduleId)

        return module.toResponse()
    }

    /**
     * Deletes module owned by instructor.
     */
    @Transactional
    fun deleteModule(moduleId: Long) {

        val email = SecurityContextHolder.getContext().authentication.name

        log.info("Instructor {} attempting to delete module {}", email, moduleId)

        val module = moduleRepository.findById(moduleId)
            .orElseThrow { ResourceNotFoundException("Module not found") }

        if (module.course.instructor.email != email) {
            log.warn("Unauthorized module delete attempt by {}", email)
            throw AccessDeniedException("You can only delete your own module")
        }

        moduleRepository.delete(module)

        log.info("Module {} deleted successfully", moduleId)
    }

    private fun Module.toResponse(): ModuleResponse {
        return ModuleResponse(
            id = this.id,
            name = this.name,
            materialUrl = this.materialUrl,
            courseId = this.course.id,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
