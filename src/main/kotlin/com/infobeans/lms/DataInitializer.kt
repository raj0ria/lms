package com.infobeans.lms

import com.infobeans.lms.enums.EnrollmentStatus
import com.infobeans.lms.enums.Role
import com.infobeans.lms.model.Course
import com.infobeans.lms.model.Enrollment
import com.infobeans.lms.model.Module
import com.infobeans.lms.model.StudentEnrollmentStatus
import com.infobeans.lms.model.User
import com.infobeans.lms.persistence.*
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val moduleRepository: ModuleRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val statusRepository: StudentEnrollmentStatusRepository,
    private val encoder: PasswordEncoder
): CommandLineRunner {
    override fun run(vararg args: String?) {

        if (userRepository.count() > 0) {
            return
        }

        // Create Instructor
        val instructor = userRepository.save(
            User(
                name = "John Instructor",
                email = "john@lms.com",
                password = encoder.encode("user123"),
                role = Role.INSTRUCTOR
            )
        )

        // Create Student
        val student = userRepository.save(
            User(
                name = "Alice Student",
                email = "alice@lms.com",
                password = encoder.encode("user123"),
                role = Role.STUDENT
            )
        )

        // Create Course
        val course = Course(
            title = "Spring Boot Masterclass",
            description = "Complete Spring Boot Course",
            published = true,
            capacity = 100
        )

        course.instructor = instructor

        val savedCourse = courseRepository.save(course)

        // Create Modules
        val module1 = Module(
            name = "Introduction",
            materialUrl = "https://example.com/intro"
        )
        module1.course = savedCourse

        val module2 = Module(
            name = "JPA Deep Dive",
            materialUrl = "https://example.com/jpa"
        )
        module2.course = savedCourse

        moduleRepository.saveAll(listOf(module1, module2))

        // Create Enrollment
        val enrollment = Enrollment()
        enrollment.user = student
        enrollment.course = savedCourse
        enrollmentRepository.save(enrollment)

        // Create Module Progress
        val status1 = StudentEnrollmentStatus(
            status = EnrollmentStatus.IN_PROGRESS
        )

        status1.enrollment = enrollment
        status1.module = module1

        val status2 = StudentEnrollmentStatus(
            status = EnrollmentStatus.NOT_STARTED
        )

        status2.enrollment = enrollment
        status2.module = module2

        statusRepository.saveAll(listOf(status1, status2))
    }
}