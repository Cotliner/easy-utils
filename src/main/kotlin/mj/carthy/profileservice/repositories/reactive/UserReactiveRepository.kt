package mj.carthy.profileservice.repositories.reactive

import mj.carthy.profileservice.entities.user.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono
import java.util.*

interface UserReactiveRepository: ReactiveMongoRepository<User, UUID> {
    fun findByUsername(username: String): Mono<User>
    fun findByUsernameIgnoreCase(username: String): Mono<User>
}