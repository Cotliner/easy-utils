package mj.carthy.easysecurity.configuration

import mj.carthy.easysecurity.manager.AuthenticationManager
import mj.carthy.easysecurity.repositories.SecurityContextRepository
import mj.carthy.easysecurity.service.JwtAuthenticateTokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Configuration
@ConditionalOnProperty(value = ["security.web.enabled"], matchIfMissing = true)
class DefaultServiceSecurityConfiguration @Autowired constructor(
        private val jwtAuthenticateTokenService: JwtAuthenticateTokenService
): BaseSecurityConfiguration() {

    @Bean fun authenticationManager(): AuthenticationManager = AuthenticationManager(jwtAuthenticateTokenService)

    @Bean fun securityContextRepository(): SecurityContextRepository = SecurityContextRepository(authenticationManager())

    override fun securityWebFilterChain(
            http: ServerHttpSecurity
    ): SecurityWebFilterChain = super.securityWebFilterChain(http.authenticationManager(
            authenticationManager()
    ).securityContextRepository(securityContextRepository()).authorizeExchange()
            .pathMatchers("/api/mocks/**").permitAll()
            .pathMatchers("/api/**").authenticated()
            .anyExchange().authenticated().and())
}