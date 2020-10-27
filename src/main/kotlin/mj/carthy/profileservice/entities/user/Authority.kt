package mj.carthy.profileservice.entities.user

import mj.carthy.profileservice.entities.user.enums.UserRole
import org.springframework.security.core.GrantedAuthority

data class Authority(val role: UserRole): GrantedAuthority {
    override fun getAuthority(): String = role.name
}