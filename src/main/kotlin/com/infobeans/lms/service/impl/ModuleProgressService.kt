package com.infobeans.lms.service.impl

import com.infobeans.lms.dto.UpdateModuleProgressRequest
import com.infobeans.lms.enums.Role
import com.infobeans.lms.exceptions.BusinessRuleViolationException
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.model.StudentEnrollmentStatus
import com.infobeans.lms.persistence.StudentEnrollmentStatusRepository
import com.infobeans.lms.persistence.UserRepository
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

/**
 * Service for managing student module progress.
 */
@Service
class ModuleProgressService(
    private val statusRepository: StudentEnrollmentStatusRepository,
    private val userRepository: UserRepository,
    private val entityManager: EntityManager
) {

    private val log = LoggerFactory.getLogger(ModuleProgressService::class.java)

    /**
     * Update module progress status for authenticated student.
     */
    @Transactional
    fun updateProgress(
        moduleId: Long,
        request: UpdateModuleProgressRequest
    ) {

        val email = SecurityContextHolder.getContext()
            .authentication.name

        val student = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException("User not found")

        if (student.role != Role.STUDENT) {
            throw AccessDeniedException("Only students can update module progress")
        }

        val projection = statusRepository.findForStudent(moduleId, student.id)
            ?: throw BusinessRuleViolationException("You are not enrolled in this module")

        if (!projection.status.canTransitionTo(request.status)) {
            throw BusinessRuleViolationException(
                "Invalid status transition from ${projection.status} to ${request.status}"
            )
        }

        // Direct update without loading full entity
        val statusEntity = entityManager.getReference(
            StudentEnrollmentStatus::class.java,
            projection.id
        )

        statusEntity.status = request.status

        log.info(
            "Student {} updated module {} status from {} to {}",
            email,
            moduleId,
            projection.status,
            request.status
        )
    }
}
