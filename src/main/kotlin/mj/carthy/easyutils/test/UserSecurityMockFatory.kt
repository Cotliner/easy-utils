package mj.carthy.easyutils.test

import mj.carthy.easyutils.model.UserSecurity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.util.*

class UserSecurityMockFatory(): WithSecurityContextFactory<MockedUser> {

    override fun createSecurityContext(
            customUser: MockedUser
    ): SecurityContext {
        val context: SecurityContext = SecurityContextHolder.createEmptyContext()
        val principal: UserSecurity = createUserSecurity(customUser)
        val auth: Authentication = UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
        context.authentication = auth
        return context
    }

    private fun createUserSecurity(customUser: MockedUser): UserSecurity = UserSecurity(
            UUID.fromString(customUser.id),
            customUser.sexe,
            customUser.username,
            customUser.password,
            mutableSetOf(SimpleGrantedAuthority(customUser.role)),
            customUser.accountNonExpired,
            customUser.accountNonLocked,
            customUser.credentialsNonExpired,
            customUser.enabled
    )
}