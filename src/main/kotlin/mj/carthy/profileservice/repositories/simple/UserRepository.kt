package mj.carthy.profileservice.repositories.simple

import mj.carthy.profileservice.entities.user.User
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface UserRepository: MongoRepository<User, UUID>