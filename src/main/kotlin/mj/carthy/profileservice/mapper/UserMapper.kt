package mj.carthy.profileservice.mapper

import mj.carthy.easysecurity.model.UserSecurity
import mj.carthy.profileservice.entities.user.User
import mj.carthy.profileservice.entities.user.dto.UserOutputDto
import org.apache.commons.lang3.StringUtils.EMPTY

fun User.toUserOutputDto() = UserOutputDto(
        id = id!!,
        username = username,
        sexe = sexe,
        accountNonExpired = accountNonExpired,
        accountNonLocked = accountNonLocked,
        credentialsNonExpired = credentialsNonExpired,
        enabled = isEnabled,
        authorities = authorities,
        connections = connections
)

fun User.toUserSecurity() = UserSecurity(
        id = id!!,
        sexe = sexe,
        username = username,
        password = EMPTY,
        authorities = authorities,
        accountNonExpired = accountNonExpired,
        accountNonLocked = accountNonLocked,
        credentialsNonExpired = credentialsNonExpired,
        enabled = isEnabled
)