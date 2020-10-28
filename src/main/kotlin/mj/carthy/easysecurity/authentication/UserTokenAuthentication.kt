package mj.carthy.easysecurity.authentication

import mj.carthy.easysecurity.model.UserSecurity
import org.springframework.security.authentication.AbstractAuthenticationToken

class UserTokenAuthentication(
        private val userSecurity: UserSecurity,
        private val token: String
) : AbstractAuthenticationToken(userSecurity.authorities) {
    override fun getCredentials(): String = token
    override fun getPrincipal(): UserSecurity = userSecurity
    override fun isAuthenticated(): Boolean = userSecurity.isEnabled && userSecurity.authorities.isNotEmpty()
}