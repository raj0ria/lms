package com.infobeans.lms.persistence

import com.infobeans.lms.dto.projections.UserAdminProjection
import com.infobeans.lms.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository: JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

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
}