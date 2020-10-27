package mj.carthy.profileservice

import mj.carthy.easyhttphandler.EnableHttpErrorHandler
import mj.carthy.easysecurity.EnableAuditing
import mj.carthy.easysecurity.EnableSecurity
import mj.carthy.easyutils.EnablePasswordValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication


@EnableHttpErrorHandler
@EnablePasswordValidation
@EnableAuditing
@EnableSecurity
@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class ProfileServiceApplication

fun main(args: Array<String>) { runApplication<ProfileServiceApplication>(*args) }
