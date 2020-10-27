package mj.carthy.easysecurity.configuration

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

open class BaseSecurityConfiguration {
    @Bean open fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http
            .csrf().disable().formLogin().disable().httpBasic().disable()
            .exceptionHandling().authenticationEntryPoint {
                swe: ServerWebExchange,
                _: AuthenticationException -> Mono.fromRunnable { swe.response.statusCode = HttpStatus.UNAUTHORIZED }
            }.accessDeniedHandler {
                swe: ServerWebExchange,
                _: AccessDeniedException -> Mono.fromRunnable { swe.response.statusCode = HttpStatus.FORBIDDEN }
            }.and().build()
}