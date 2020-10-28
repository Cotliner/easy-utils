package mj.carthy.profileservice.entities.user.dto

import mj.carthy.easyutils.enums.Sexe
import mj.carthy.profileservice.entities.user.Authority
import mj.carthy.profileservice.entities.user.Connection
import java.util.*

data class UserOutputDto(
        val id: UUID,
        val username: String,
        val sexe: Sexe,
        val accountNonExpired: Boolean,
        val accountNonLocked: Boolean,
        val credentialsNonExpired: Boolean,
        val enabled: Boolean,
        val authorities: MutableSet<Authority> = HashSet(),
        val connections: MutableSet<Connection> = HashSet()
)
