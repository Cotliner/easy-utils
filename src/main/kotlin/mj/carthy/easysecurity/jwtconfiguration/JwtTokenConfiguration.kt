package mj.carthy.easysecurity.jwtconfiguration

import mj.carthy.easysecurity.service.JwtAuthenticateTokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(JwtSecurityProperties::class)
@Configuration class JwtTokenConfiguration @Autowired constructor(
        val jwtSecurityProperties: JwtSecurityProperties
) {
    @Bean fun jwtAuthenticateTokenService(): JwtAuthenticateTokenService = JwtAuthenticateTokenService(jwtSecurityProperties)
}