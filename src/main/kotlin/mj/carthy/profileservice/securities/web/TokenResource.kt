package mj.carthy.profileservice.securities.web

import kotlinx.coroutines.reactor.mono
import mj.carthy.easysecurity.model.Token
import mj.carthy.easysecurity.service.JwtAuthenticateTokenService
import mj.carthy.profileservice.securities.dto.UserLoginDto
import mj.carthy.profileservice.securities.services.ConnectionService
import mj.carthy.profileservice.securities.services.TokenService
import mj.carthy.profileservice.securities.web.TokenResource.Companion.URI
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import javax.validation.Valid

@RequestMapping(value = [URI], produces = [APPLICATION_JSON_VALUE])
@RestController class TokenResource(
        /*Services*/
        private val jwtAuthenticateTokenService: JwtAuthenticateTokenService,
        private val connectionService: ConnectionService,
        private val tokenService: TokenService
) {
    companion object { const val URI = "/api/v1/auth/token" }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = [APPLICATION_JSON_VALUE]) fun create (
            request: ServerHttpRequest,
            @RequestBody @Valid userLogin: UserLoginDto
    ): Mono<Token> = mono { tokenService.createToken(
            jwtAuthenticateTokenService::createToken,
            connectionService::create,
            request,
            userLogin.username,
            userLogin.password
    ) }
}