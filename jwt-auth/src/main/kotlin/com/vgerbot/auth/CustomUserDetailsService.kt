package com.vgerbot.auth

import com.vgerbot.user.dao.UserDao
import org.ktorm.dsl.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

@Component
class CustomUserDetailsService : UserDetailsService {
    @Autowired
    lateinit var userDao: UserDao;

    override fun loadUserByUsername(username: String?): UserDetails? {
        return username ?.let {
            val user = userDao.findOne { it.username eq username } ?: return null;

            return object : UserDetails {
                override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableListOf<GrantedAuthority>()

                override fun getPassword(): String = user.password

                override fun getUsername(): String = user.username

                override fun isAccountNonExpired(): Boolean = true

                override fun isAccountNonLocked(): Boolean = true

                override fun isCredentialsNonExpired(): Boolean = true

                override fun isEnabled(): Boolean = true
            }
        }
    }
}