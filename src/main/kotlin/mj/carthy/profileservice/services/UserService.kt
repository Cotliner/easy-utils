package mj.carthy.profileservice.services

import mj.carthy.easysecurity.model.UserSecurity
import mj.carthy.easyutils.helper.monoOrError
import mj.carthy.profileservice.entities.user.User
import mj.carthy.profileservice.repositories.reactive.UserReactiveRepository
import org.springframework.stereotype.Service

@Service class UserService(private val userReactiveRepository: UserReactiveRepository) {

    suspend fun getById(userSercurity: UserSecurity): User = monoOrError(userReactiveRepository::findById, User::class.java, userSercurity.id)
}
