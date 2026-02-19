package com.infobeans.lms.persistence

import com.infobeans.lms.dto.projections.AdminCourseProjection
import com.infobeans.lms.dto.projections.CourseDetailProjection
import com.infobeans.lms.dto.projections.CourseSearchProjection
import com.infobeans.lms.dto.projections.InstructorCourseProjection
import com.infobeans.lms.model.Course
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CourseRepository: JpaRepository<Course, Long> {
    fun existsByTitle(title: String): Boolean

    @Query(
        value = """
        select 
            c.id as courseId,
            c.title as title,
            c.description as description,
            i.name as instructorName,
            c.capacity as capacity,
            count(e.id) as enrolledCount,
            c.createdAt as createdAt
        from Course c
        join c.instructor i
        left join Enrollment e on e.course.id = c.id
        where c.published = true
        and (:keyword is null or lower(c.title) like lower(concat('%', :keyword, '%')))
        group by c.id, c.title, c.description, i.name, c.capacity, c.createdAt
    """,
        countQuery = """
        select count(c.id)
        from Course c
        where c.published = true
        and (:keyword is null or lower(c.title) like lower(concat('%', :keyword, '%')))
    """
    )
    fun searchPublishedCourses(
        keyword: String?,
        pageable: Pageable
    ): Page<CourseSearchProjection>

    @Query(
        value = """
        select 
            c.id as id,
            c.title as title,
            c.description as description,
            c.published as published,
            c.capacity as capacity,
            i.name as instructorName,
            c.createdAt as createdAt
        from Course c
        join c.instructor i
        where (:keyword is null or lower(c.title) like lower(concat('%', :keyword, '%')))
    """,
        countQuery = """
        select count(c.id)
        from Course c
        where (:keyword is null or lower(c.title) like lower(concat('%', :keyword, '%')))
    """
    )
    fun findAllAdminCourses(
        keyword: String?,
        pageable: Pageable
    ): Page<AdminCourseProjection>


    /**
     * Fetches the course created by an instructor
     */
    @Query(
        value = """
        select 
            c.id as id,
            c.title as title,
            c.description as description,
            c.published as published,
            c.capacity as capacity,
            c.createdAt as createdAt
        from Course c
        where c.instructor.id = :instructorId
    """,
        countQuery = """
        select count(c.id)
        from Course c
        where c.instructor.id = :instructorId
    """
    )
    fun findByInstructorId(
        instructorId: Long,
        pageable: Pageable
    ): Page<InstructorCourseProjection>


    @Query("""
    select 
        c.id as id,
        c.title as title,
        c.description as description,
        c.capacity as capacity,
        c.published as published,
        i.id as instructorId,
        i.name as instructorName,
        i.email as instructorEmail,
        c.createdAt as createdAt,
        c.updatedAt as updatedAt
    from Course c
    join c.instructor i
    where c.id = :courseId
""")
    fun findCourseDetail(courseId: Long): CourseDetailProjection?




}