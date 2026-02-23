package com.infobeans.lms.persistence

import com.infobeans.lms.dto.projections.ModuleStatusProjection
import com.infobeans.lms.model.StudentEnrollmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StudentEnrollmentStatusRepository : JpaRepository<StudentEnrollmentStatus, Long> {
    @Query("""
        select s.id as id,
               s.status as status,
               e.user.id as enrollmentUserId
        from StudentEnrollmentStatus s
        join s.enrollment e
        where s.module.id = :moduleId
          and e.user.id = :userId
    """)
    fun findForStudent(
        moduleId: Long,
        userId: Long
    ): ModuleStatusProjection?


//    fun findByStudentIdAndModuleId(
//        studentId: Long,
//        moduleId: Long
//    ): StudentEnrollmentStatus?

    fun findByEnrollmentId(enrollmentId: Long): List<StudentEnrollmentStatus>
}