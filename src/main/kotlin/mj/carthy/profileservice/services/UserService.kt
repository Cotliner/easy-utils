package mj.carthy.profileservice.services

import kotlinx.coroutines.reactive.awaitSingle
import mj.carthy.easysecurity.model.UserSecurity
import mj.carthy.easyutils.helper.monoOrError
import mj.carthy.profileservice.entities.user.User
import mj.carthy.profileservice.repositories.reactive.UserReactiveRepository
import mj.carthy.profileservice.securities.scope.Scope.Companion.me
import org.springframework.stereotype.Service
import java.util.stream.Collectors.toSet

@Service class UserService(
        /* Repositories */
        private val userReactiveRepository: UserReactiveRepository
) {

    suspend fun getById(userSercurity: UserSecurity): User = monoOrError(userReactiveRepository::findById, User::class.java, userSercurity.id)

    suspend fun getAll(userSecurity: UserSecurity): MutableSet<User> = userReactiveRepository.findAll().filter {
        elem -> me(userSecurity, elem)
    }.collect(toSet()).awaitSingle()
}
