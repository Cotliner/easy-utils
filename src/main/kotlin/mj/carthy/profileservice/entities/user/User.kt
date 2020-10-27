package mj.carthy.profileservice.entities.user

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import mj.carthy.easyutils.document.BaseDocument
import mj.carthy.easyutils.enums.Sexe
import mj.carthy.profileservice.entities.user.enums.CodeReason
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.lang.Boolean.FALSE
import java.util.*
import javax.validation.constraints.Email
import kotlin.collections.HashSet

@Document data class User(
        @Email @Indexed(unique = true) private val username: String,
        @JsonIgnore @JsonIgnoreProperties private val password: String,
        val sexe: Sexe,
        val accountNonExpired: Boolean = FALSE,
        val accountNonLocked: Boolean = FALSE,
        val credentialsNonExpired: Boolean = FALSE,
        val authorities: MutableSet<Authority> = HashSet(),
        val connections: MutableSet<Connection> = HashSet(),
        val codeByReasons: MutableMap<CodeReason, Code> = EnumMap(CodeReason::class.java)
): BaseDocument<UUID>(), UserDetails {
        override fun getUsername(): String = username
        override fun getPassword(): String = password
        override fun isAccountNonExpired(): Boolean = isAccountNonExpired
        override fun isAccountNonLocked(): Boolean = isAccountNonLocked
        override fun isCredentialsNonExpired(): Boolean = isCredentialsNonExpired
        override fun isEnabled(): Boolean = accountNonLocked && accountNonExpired
        override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities
}