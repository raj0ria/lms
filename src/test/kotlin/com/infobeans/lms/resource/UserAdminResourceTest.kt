package com.infobeans.lms.resource

import com.infobeans.lms.dto.*
import com.infobeans.lms.enums.Role
import com.infobeans.lms.service.impl.UserAdminService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import io.mockk.junit5.MockKExtension
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class UserAdminResourceTest {

    @MockK
    lateinit var userAdminService: UserAdminService

    @InjectMockKs
    lateinit var resource: UserAdminResource

    @Test
    fun `should create user`() {
        val request = CreateUserRequest(
            name = "John",
            email = "john@mail.com",
            password = "pass",
            role = Role.STUDENT
        )

        val dto = UserAdminResponse(
            id = 1L,
            name = "John",
            email = "john@mail.com",
            role = Role.STUDENT,
            createdAt = Instant.now()
        )

        every { userAdminService.createUser(request) } returns dto

        val response = resource.createUser(request)

        assertEquals(201, response.statusCode.value())
        assertEquals("John", response.body!!.name)
    }

    @Test
    fun `should return paginated users`() {
        val pageable: Pageable = PageRequest.of(0, 10)

        val user = UserAdminResponse(
            id = 1L,
            name = "John",
            email = "john@mail.com",
            role = Role.STUDENT,
            createdAt = Instant.now()
        )

        val paged = PagedResponse(
            page = 0,
            size = 10,
            totalElements = 1,
            totalPages = 1,
            content = listOf(user)
        )

        every { userAdminService.getUsers(null, pageable) } returns paged

        val response = resource.getUsers(null, pageable)

        assertEquals(200, response.statusCode.value())
        assertEquals(1, response.body!!.totalElements)
    }

    @Test
    fun `should return user by id`() {
        val dto = UserAdminResponse(
            id = 1L,
            name = "John",
            email = "john@mail.com",
            role = Role.STUDENT,
            createdAt = Instant.now()
        )

        every { userAdminService.getUserById(1L) } returns dto

        val response = resource.getUserById(1L)

        assertEquals(200, response.statusCode.value())
        assertEquals("John", response.body!!.name)
    }

    @Test
    fun `should update user`() {
        val request = UpdateUserRequest(
            name = "Updated",
            role = Role.INSTRUCTOR
        )

        val dto = UserAdminResponse(
            id = 1L,
            name = "Updated",
            email = "john@mail.com",
            role = Role.INSTRUCTOR,
            createdAt = Instant.now()
        )

        every { userAdminService.updateUser(1L, request) } returns dto

        val response = resource.updateUser(1L, request)

        assertEquals("Updated", response.body!!.name)
    }

    @Test
    fun `should delete user`() {
        every { userAdminService.deleteUser(1L) } just Runs

        val response: ResponseEntity<Void> = resource.deleteUser(1L)

        assertEquals(204, response.statusCode.value())
        verify { userAdminService.deleteUser(1L) }
    }
}