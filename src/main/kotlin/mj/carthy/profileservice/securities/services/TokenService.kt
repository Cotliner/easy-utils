package mj.carthy.profileservice.securities.services

import com.google.common.annotations.VisibleForTesting
import kotlinx.coroutines.reactive.awaitSingle
import mj.carthy.easysecurity.model.Token
import mj.carthy.easysecurity.model.UserSecurity
import mj.carthy.easyutils.exception.AccessDeniedException
import mj.carthy.easyutils.exception.UnprocessableEntityException
import mj.carthy.profileservice.entities.user.Connection
import mj.carthy.profileservice.entities.user.User
import mj.carthy.profileservice.entities.user.enums.CodeReason.EMAIL_VERIFICATION
import mj.carthy.profileservice.helper.ErrorService
import mj.carthy.profileservice.helper.ErrorService.Companion.AUTHENTICATION_DENIED
import mj.carthy.profileservice.helper.ErrorService.Companion.EMAIL_VERIFICATION_FAILED
import mj.carthy.profileservice.mapper.toUserSecurity
import mj.carthy.profileservice.repositories.reactive.UserReactiveRepository
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.lang.String.format
import java.util.*

@Service class TokenService(
        /* Clients */
        private val bCryptPasswordEncoder: BCryptPasswordEncoder,
        /* Repositories */
        private val userReactiveRepository: UserReactiveRepository
) {
    companion object {
        const val USERNAME_OR_PASSWORD_NOT_VALID = "Username %s or password is not valid"
        const val VERIFY_YOUR_EMAIL = "Verify your email"
        const val USER_NOT_ENABLE = "User is not enable"
    }

    suspend fun createToken(
            tokenCreator: (id:UUID, user: UserSecurity) -> Token,
            connectionCreator: (isValidPassword: Boolean, request: ServerHttpRequest) -> Connection,
            request: ServerHttpRequest,
            username: String,
            password: String
    ): Token {
        val user: User = userReactiveRepository.findByUsername(username).awaitSingle() ?: invalidAuth(username)

        val isValidPassword = bCryptPasswordEncoder.matches(password, user.password)

        user.connections.add(connectionCreator.invoke(isValidPassword, request))
        val updatedUser: User = userReactiveRepository.save(user).awaitSingle()

        if (!isValidPassword) invalidAuth(username)

        verifyUserIsEnable(updatedUser)

        return tokenCreator.invoke(updatedUser.id!!, updatedUser.toUserSecurity())
    }

    @VisibleForTesting fun invalidAuth(username: String): Nothing = throw AccessDeniedException(format(
            USERNAME_OR_PASSWORD_NOT_VALID, username),
            AUTHENTICATION_DENIED
    )

    @VisibleForTesting fun verifyUserIsEnable(user: User) {
        if (user.codeByReasons.containsKey(EMAIL_VERIFICATION)) {
            throw UnprocessableEntityException(VERIFY_YOUR_EMAIL, EMAIL_VERIFICATION_FAILED);
        } else if (!user.isEnabled) {
            throw UnprocessableEntityException(USER_NOT_ENABLE, ErrorService.USER_NOT_ENABLE);
        }
    }
}