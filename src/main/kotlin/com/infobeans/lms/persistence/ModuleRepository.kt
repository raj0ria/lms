package com.infobeans.lms.persistence

import com.infobeans.lms.dto.projections.CourseModuleProjection
import com.infobeans.lms.dto.projections.InstructorModuleProjection
import com.infobeans.lms.model.Course
import com.infobeans.lms.model.Module
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ModuleRepository: JpaRepository<Module, Long> {
    fun existsByCourseIdAndName(courseId: Long, name: String): Boolean

    @Query(
        value = """
        select 
            m.id as id,
            m.name as name,
            m.materialUrl as materialUrl,
            m.createdAt as createdAt
        from Module m
        where m.course.id = :courseId
    """,
        countQuery = """
        select count(m.id)
        from Module m
        where m.course.id = :courseId
    """
    )
    fun findByCourseId(
        courseId: Long,
        pageable: Pageable
    ): Page<InstructorModuleProjection>

    @Query("""
    select 
        m.id as id,
        m.name as name,
        m.materialUrl as materialUrl,
        m.createdAt as createdAt
    from Module m
    where m.course.id = :courseId
""")
    fun findModulesByCourseId(courseId: Long): List<CourseModuleProjection>


}