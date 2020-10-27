package mj.carthy.profileservice.configuration

import com.google.common.annotations.VisibleForTesting
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER
import io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration class Documentation {

    companion object {
        const val BEARER_KEY = "bearer-jwt"
        const val BEARER = "bearer"
        const val JWT = "JWT"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val READ = "read"
        const val WRITE = "write"

        const val PRIVATE_LICENCE = "Private"
    }

    @Bean fun openApi(
            @Value("\${springdoc.info.title}") title: String,
            @Value("\${springdoc.info.version}") appVersion: String,
            @Value("\${springdoc.info.description}") description: String
    ): OpenAPI = OpenAPI().components(
            securityComponent()
    ).info(apiInfo(
            title,
            appVersion,
            description
    )).addSecurityItem(SecurityRequirement().addList(BEARER_KEY, listOf(READ, WRITE)))

    @VisibleForTesting fun securityComponent(): Components = Components().addSecuritySchemes(BEARER_KEY, createSecurityScheme())

    @VisibleForTesting fun createSecurityScheme(): SecurityScheme = SecurityScheme().type(HTTP).scheme(
            BEARER
    ).bearerFormat(JWT).`in`(HEADER).name(AUTHORIZATION_HEADER)

    @VisibleForTesting fun apiInfo(title: String, appVersion: String, description: String): Info = Info().title(
            title
    ).version(appVersion).license(License().name(PRIVATE_LICENCE))
}