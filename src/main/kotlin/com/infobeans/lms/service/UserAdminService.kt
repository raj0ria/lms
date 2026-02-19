package com.infobeans.lms.service

import com.infobeans.lms.dto.CreateUserRequest
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.UpdateUserRequest
import com.infobeans.lms.dto.UserAdminResponse
import com.infobeans.lms.exceptions.BusinessRuleViolationException
import com.infobeans.lms.exceptions.ResourceNotFoundException
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.UserRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Service responsible for administrative user management.
 */
@Service
class UserAdminService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val log = LoggerFactory.getLogger(UserAdminService::class.java)

    /**
     * Creates a new user.
     */
    @Transactional
    fun createUser(request: CreateUserRequest): UserAdminResponse {

        log.info("Admin attempting to create user with email={}", request.email)

        if (userRepository.findByEmail(request.email) != null) {
            log.warn("Duplicate user creation attempt email={}", request.email)
            throw BusinessRuleViolationException("Email already exists")
        }

        val user = User(
            name = request.name.trim(),
            email = request.email.lowercase(),
            password = passwordEncoder.encode(request.password),
            role = request.role
        )

        val saved = userRepository.save(user)

        log.info("User created successfully id={}", saved.id)

        return saved.toResponse()
    }

    /**
     * Retrieves paginated list of users.
     */
    @Transactional
    fun getUsers(
        keyword: String?,
        pageable: Pageable
    ): PagedResponse<UserAdminResponse> {

        log.info(
            "Admin fetching users keyword={} page={} size={}",
            keyword,
            pageable.pageNumber,
            pageable.pageSize
        )

        val page = userRepository.searchUsers(keyword, pageable)

        val content = page.content.map {
            UserAdminResponse(
                id = it.id,
                name = it.name,
                email = it.email,
                role = it.role,
                createdAt = it.createdAt
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
     * Retrieves single user by ID.
     */
    @Transactional
    fun getUserById(id: Long): UserAdminResponse {

        log.info("Admin fetching user id={}", id)

        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found") }

        return user.toResponse()
    }

    /**
     * Updates user details.
     */
    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequest): UserAdminResponse {

        log.info("Admin updating user id={}", id)

        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found") }

        request.name?.let { user.name = it.trim() }
        request.role?.let { user.role = it }

        log.info("User id={} updated successfully", id)

        return user.toResponse()
    }

    /**
     * Deletes user by ID.
     */
    @Transactional
    fun deleteUser(id: Long) {

        log.info("Admin deleting user id={}", id)

        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found") }

        userRepository.delete(user)

        log.info("User id={} deleted successfully", id)
    }

    private fun User.toResponse(): UserAdminResponse =
        UserAdminResponse(
            id = this.id,
            name = this.name,
            email = this.email,
            role = this.role,
            createdAt = this.createdAt
        )
}
