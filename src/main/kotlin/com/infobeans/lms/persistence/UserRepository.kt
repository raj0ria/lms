package com.infobeans.lms.persistence

import com.infobeans.lms.dto.projections.AdminDashboardSummaryProjection
import com.infobeans.lms.dto.projections.UserAdminProjection
import com.infobeans.lms.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Repository for managing User entities.
 *
 * Responsibilities:
 * - Fetch users by email (unique index enforced)
 * - Provide paginated & filtered search for admin panel
 *
 * Layer: Repository (JPA)
 */


interface UserRepository: JpaRepository<User, Long> {

    /**
     * Finds user by unique email.
     *
     * @param email user email
     * @return User or null if not found
     */
    fun findByEmail(email: String): User?


    /**
     * Searches users by keyword (name or email).
     *
     * Supports:
     * - Pagination
     * - Case-insensitive filtering
     * - Admin panel usage
     *
     * @param keyword optional search keyword
     * @param pageable pagination configuration
     * @return paginated projection of users
     */
    @Query(
        value = """
            select 
                u.id as id,
                u.name as name,
                u.email as email,
                u.role as role,
                u.createdAt as createdAt
            from User u
            where (:keyword is null or 
                   lower(u.name) like lower(concat('%', :keyword, '%')) or
                   lower(u.email) like lower(concat('%', :keyword, '%')))
        """,
        countQuery = """
            select count(u.id)
            from User u
            where (:keyword is null or 
                   lower(u.name) like lower(concat('%', :keyword, '%')) or
                   lower(u.email) like lower(concat('%', :keyword, '%')))
        """
    )
    fun searchUsers(
        keyword: String?,
        pageable: Pageable
    ): Page<UserAdminProjection>


    @Query(
        value = """
        select 
            (select count(*) from users where role = 'STUDENT') as totalStudents,
            (select count(*) from users where role = 'INSTRUCTOR') as totalInstructors,
            (select count(*) from courses) as totalCourses,
            (select count(*) from enrollments) as totalEnrollments
    """,
        nativeQuery = true
    )
    fun fetchDashboardSummary(): AdminDashboardSummaryProjection
}