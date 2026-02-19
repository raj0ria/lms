package com.infobeans.lms.persistence

import com.infobeans.lms.dto.projections.CourseEnrollmentUserProjection
import com.infobeans.lms.dto.projections.InstructorCourseStudentProjection
import com.infobeans.lms.dto.projections.StudentCourseDashboardProjection
import com.infobeans.lms.model.Enrollment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EnrollmentRepository: JpaRepository<Enrollment, Long> {
    fun existsByUserIdAndCourseId(userId: Long, courseId: Long): Boolean

    fun countByCourseId(courseId: Long): Long

    fun findByUserIdAndCourseId(userId: Long, courseId: Long): Enrollment?

    @Query(
        value = """
        select 
            c.id as courseId,
            c.title as courseTitle,
            e.enrolledAt as enrolledAt,
            count(ms.id) as totalModules,
            sum(case when ms.status = 'COMPLETED' then 1 else 0 end) as completedModules
        from Enrollment e
        join e.course c
        join e.moduleStatuses ms
        where e.user.id = :userId
        group by c.id, c.title, e.enrolledAt
    """,
        countQuery = """
        select count(e.id)
        from Enrollment e
        where e.user.id = :userId
    """
    )
    fun findStudentDashboard(
        userId: Long,
        pageable: Pageable
    ): Page<StudentCourseDashboardProjection>

    @Query(
        value = """
        select 
            u.id as studentId,
            u.name as studentName,
            u.email as studentEmail,
            e.enrolledAt as enrolledAt,
            count(ms.id) as totalModules,
            sum(case when ms.status = 'COMPLETED' then 1 else 0 end) as completedModules
        from Enrollment e
        join e.user u
        join e.moduleStatuses ms
        where e.course.id = :courseId
        group by u.id, u.name, u.email, e.enrolledAt
    """,
        countQuery = """
        select count(e.id)
        from Enrollment e
        where e.course.id = :courseId
    """
    )
    fun findInstructorCourseStudents(
        courseId: Long,
        pageable: Pageable
    ): Page<InstructorCourseStudentProjection>


    @Query("""
    select 
        u.id as userId,
        u.name as name,
        u.email as email,
        u.role as role,
        e.enrolledAt as enrolledAt
    from Enrollment e
    join e.user u
    where e.course.id = :courseId
""")
    fun findEnrolledUsersByCourseId(
        courseId: Long
    ): List<CourseEnrollmentUserProjection>



}