package com.infobeans.lms.service.impl

import com.infobeans.lms.persistence.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userPersistence: UserRepository
) : UserDetailsService{
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userPersistence.findByEmail(email) ?:
                    throw UsernameNotFoundException("User not found")
        return User(
                user.email,
                user.password,
            listOf(SimpleGrantedAuthority("ROLE_${user.role}"))
            )
    }
}