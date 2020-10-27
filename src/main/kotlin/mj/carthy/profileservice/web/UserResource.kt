package mj.carthy.profileservice.web

import kotlinx.coroutines.reactor.mono
import mj.carthy.easysecurity.model.UserSecurity
import mj.carthy.profileservice.entities.user.User
import mj.carthy.profileservice.securities.scope.AdminOrMe
import mj.carthy.profileservice.securities.scope.AllowAll
import mj.carthy.profileservice.securities.scope.editor.UserEditor
import mj.carthy.profileservice.services.UserService
import mj.carthy.profileservice.web.UserResource.Companion.URI
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.support.WebExchangeDataBinder
import reactor.core.publisher.Mono
import java.util.*

@RequestMapping(value = [URI], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController class UserResource(
        /* Editor */
        private val editor: UserEditor,
        /* User Service */
        private val userService: UserService
) {
    companion object { const val URI = "/api/v1/users" }

    @InitBinder fun dataBinding(binder: WebExchangeDataBinder) {
        binder.registerCustomEditor(User::class.java, editor)
    }

    @AdminOrMe
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("{id}")
    fun getById(@PathVariable id: UUID, @PathVariable("id") document: User): User = document

    @AllowAll
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    fun getCurrent(@AuthenticationPrincipal userSecurity: UserSecurity): Mono<User> = mono { userService.getById(userSecurity) }
}