package mj.carthy.easysecurity.manager

import mj.carthy.easysecurity.authentication.UserTokenAuthentication
import mj.carthy.easysecurity.model.UserSecurity
import mj.carthy.easysecurity.service.JwtAuthenticateTokenService
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

class AuthenticationManager(private val jwtAuthenticateTokenService: JwtAuthenticateTokenService): ReactiveAuthenticationManager {
    override fun authenticate(
            authentication: Authentication
    ): Mono<Authentication> = Mono.justOrEmpty(
            authentication.credentials.toString()
    ).switchIfEmpty(Mono.empty()).map(
            jwtAuthenticateTokenService::createUserSecurityFromToken
    ).map { userSecurity: UserSecurity -> UserTokenAuthentication(userSecurity, authentication.credentials.toString()) }
}