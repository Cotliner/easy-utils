package mj.carthy.profileservice.securities.scope

import com.google.common.annotations.VisibleForTesting
import mj.carthy.easysecurity.model.UserSecurity
import mj.carthy.easyutils.document.BaseDocument
import mj.carthy.profileservice.entities.user.User
import mj.carthy.profileservice.entities.user.enums.UserRole
import org.springframework.security.core.GrantedAuthority
import java.util.*
import kotlin.reflect.KFunction

abstract class Scope {
    companion object {
        private const val ME = "T(mj.carthy.profileservice.securities.scope.Scope).me(principal, #document)"
        const val ADMIN = "T(mj.carthy.profileservice.securities.scope.Scope).admin(principal)"
        const val ADMIN_OR_ME = "$ADMIN||$ME"

        @VisibleForTesting fun admin(userSecurity: UserSecurity): Boolean = isUserAdmin(userSecurity)

        @VisibleForTesting fun me(user: UserSecurity, document: User): Boolean = user.id == document.id

        fun <T: BaseDocument<UUID>> adminOrMe(
                idGetter: KFunction<UUID>,
                user: UserSecurity,
                document: T
        ): Boolean = isUserAdmin(user) || user.id == idGetter.call(document)

        @VisibleForTesting fun isUserAdmin(userSecurity: UserSecurity) = userSecurity.authorities.stream().map {
            it as GrantedAuthority
        }.map { elem -> elem.authority }.anyMatch { elem -> UserRole.ADMIN.name == elem }
    }
}