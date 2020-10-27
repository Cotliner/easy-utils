package mj.carthy.profileservice.securities.configuration

import mj.carthy.easysecurity.configuration.BaseSecurityConfiguration
import mj.carthy.easysecurity.manager.AuthenticationManager
import mj.carthy.easysecurity.repositories.SecurityContextRepository
import mj.carthy.easysecurity.service.JwtAuthenticateTokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@ConditionalOnWebApplication
@Configuration class SecurityConfiguration(
        /*Properties*/
        @Value("\${springdoc.authorize.path}") val authorizeSwaggerPath: Array<String>,
        /*Services*/
        val jwtAuthenticateTokenService: JwtAuthenticateTokenService
): BaseSecurityConfiguration() {

    @Bean fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean fun authenticationManager(): AuthenticationManager = AuthenticationManager(jwtAuthenticateTokenService)

    @Bean fun securityContextRepository(): SecurityContextRepository = SecurityContextRepository(authenticationManager())

    override fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = super.securityWebFilterChain(http
            .requestCache().disable()
            .cors().disable()
            .headers().disable()
            .authenticationManager(authenticationManager())
            .securityContextRepository(securityContextRepository())
            .authorizeExchange()
            .pathMatchers(HttpMethod.POST, "/api/v1/auth/token").permitAll()
            .pathMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
            .pathMatchers(HttpMethod.PUT, "/api/v1/users/password").permitAll()
            .pathMatchers(HttpMethod.POST, "/api/v1/users/code").permitAll()
            .pathMatchers(HttpMethod.PUT, "/api/v1/users/code").permitAll()
            .pathMatchers(HttpMethod.GET, "/api/v1/items").permitAll() /* Events */
            .pathMatchers(HttpMethod.GET, "/api/v1/stats/profile/count").permitAll()
            .pathMatchers(*authorizeSwaggerPath).permitAll()
            .anyExchange().authenticated()
            .and())
}