package com.infobeans.lms.resource

import com.infobeans.lms.dto.ChangePasswordRequest
import com.infobeans.lms.dto.UserProfileResponse
import com.infobeans.lms.enums.Role
import com.infobeans.lms.service.impl.UserProfileService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.junit5.MockKExtension
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class UserProfileResourceTest {

    @MockK
    lateinit var service: UserProfileService

    @InjectMockKs
    lateinit var resource: UserProfileResource

    @Test
    fun `should return profile`() {
        val dto = UserProfileResponse(
            id = 1L,
            name = "John",
            email = "john@mail.com",
            role = Role.STUDENT,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { service.getCurrentUserProfile() } returns dto

        val response: ResponseEntity<UserProfileResponse> =
            resource.getProfile()

        assertEquals(200, response.statusCode.value())
        assertEquals("John", response.body!!.name)
    }

    @Test
    fun `should change password`() {
        val request = ChangePasswordRequest("old", "new")

        every { service.changePassword(request) } just Runs

        val response: ResponseEntity<Void> =
            resource.changePassword(request)

        assertEquals(204, response.statusCode.value())
    }
}