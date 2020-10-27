package mj.carthy.profileservice.configuration

import mj.carthy.easyutils.enums.Sexe
import mj.carthy.profileservice.entities.user.Authority
import mj.carthy.profileservice.entities.user.User
import mj.carthy.profileservice.entities.user.enums.UserRole
import mj.carthy.profileservice.repositories.reactive.UserReactiveRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.lang.Boolean.TRUE

@Profile("default", "integration")
@Component class Runtime(
        /* Client */
        private val bCryptPasswordEncoder: BCryptPasswordEncoder,
        /* Repositories */
        private val userReactiveRepository: UserReactiveRepository
): CommandLineRunner {
    override fun run(vararg args: String?) {
        val johnDoe = "john.doe@yopmail.com"

        val password = bCryptPasswordEncoder.encode("doe")
        val user = User(
                username = johnDoe,
                password = password,
                sexe = Sexe.MALE,
                authorities = mutableSetOf(Authority(UserRole.ADMIN)),
                accountNonLocked = TRUE,
                accountNonExpired = TRUE
        )

        userReactiveRepository.findByUsernameIgnoreCase(johnDoe).switchIfEmpty(userReactiveRepository.save(user)).subscribe()
    }
}