package com.infobeans.lms.resource

import com.infobeans.lms.dto.CreateUserRequest
import com.infobeans.lms.dto.PagedResponse
import com.infobeans.lms.dto.UpdateUserRequest
import com.infobeans.lms.dto.UserAdminResponse
import com.infobeans.lms.service.UserAdminService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST resource for administrative user management.
 */
@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
class UserAdminResource(
    private val userAdminService: UserAdminService
) {

    private val log = LoggerFactory.getLogger(UserAdminResource::class.java)

    /**
     * Creates a new user.
     */
    @PostMapping
    fun createUser(
        @RequestBody request: CreateUserRequest
    ): ResponseEntity<UserAdminResponse> {

        log.info("POST /api/v1/users invoked")

        val response = userAdminService.createUser(request)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Returns paginated users.
     */
    @GetMapping
    fun getUsers(
        @RequestParam(required = false) keyword: String?,
        @PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<PagedResponse<UserAdminResponse>> {

        log.info("GET /api/v1/users invoked")

        val response = userAdminService.getUsers(keyword, pageable)

        return ResponseEntity.ok(response)
    }

    /**
     * Returns user by ID.
     */
    @GetMapping("/{id}")
    fun getUserById(
        @PathVariable id: Long
    ): ResponseEntity<UserAdminResponse> {

        val response = userAdminService.getUserById(id)

        return ResponseEntity.ok(response)
    }

    /**
     * Updates user.
     */
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserAdminResponse> {

        val response = userAdminService.updateUser(id, request)

        return ResponseEntity.ok(response)
    }

    /**
     * Deletes user.
     */
    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        userAdminService.deleteUser(id)

        return ResponseEntity.noContent().build()
    }
}
