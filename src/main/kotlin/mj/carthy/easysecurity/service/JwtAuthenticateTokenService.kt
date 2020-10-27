package mj.carthy.easysecurity.service

import com.google.common.annotations.VisibleForTesting
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import mj.carthy.easysecurity.jwtconfiguration.JwtSecurityProperties
import mj.carthy.easysecurity.model.Token
import mj.carthy.easysecurity.model.UserSecurity
import mj.carthy.easyutils.enums.Sexe
import org.apache.commons.lang3.StringUtils.EMPTY
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Collectors

@Service class JwtAuthenticateTokenService(val jwtSecurityProperties: JwtSecurityProperties) {
    companion object {
        const val ID = "id"
        const val USERNAME = "username"
        const val SEXE = "sexe"
        const val ACCOUNT_NON_EXPIRED = "accountNonExpired"
        const val ACCOUNT_NON_LOCKED = "accountNonLocked"
        const val CREDENTIALS_NON_EXPIRED = "credentialsNonExpired"
        const val ENABLE = "enable"
        const val TOKEN_CREATE_TIME = "TOKEN_CREATE_TIME"
        const val ROLES = "roles"
    }

    fun createUserSecurityFromToken(token: String): UserSecurity {
        val claimsJws: Jws<Claims> = Jwts.parser().setSigningKey(jwtSecurityProperties.signingKey).parseClaimsJws(token)
        val body: Claims = claimsJws.body
        val id = UUID.fromString(body.subject)
        val username: String = body.get(USERNAME, String::class.java)
        val sexe: Sexe = Sexe.valueOf(body.get(SEXE, String::class.java))
        val accountNonExpired: Boolean = body.get(ACCOUNT_NON_EXPIRED, Object::class.java).toString().toBoolean()
        val accountNonLocked: Boolean = body.get(ACCOUNT_NON_LOCKED, Object::class.java).toString().toBoolean()
        val credentialsNonExpired: Boolean = body.get(CREDENTIALS_NON_EXPIRED, Object::class.java).toString().toBoolean()
        val enable: Boolean = body.get(ENABLE, Object::class.java).toString().toBoolean()
        val roles: MutableSet<*> = body.get(ROLES, MutableList::class.java).toMutableSet()
        val authorities: MutableSet<GrantedAuthority> = roles.stream().map { elem -> elem as String
        }.map { elem -> SimpleGrantedAuthority(elem) }.collect(Collectors.toSet())
        return UserSecurity(id, sexe, username, EMPTY, authorities, accountNonExpired, accountNonLocked, credentialsNonExpired, enable)
    }

    fun createToken(id: UUID, user: UserSecurity): Token {
        val roles = user.authorities.stream().map { elem: GrantedAuthority -> elem.authority }.collect(Collectors.toSet())
        val expiryTime = Instant.now().plus(jwtSecurityProperties.validity, jwtSecurityProperties.unit)

        val token: String = Jwts.builder().signWith(SignatureAlgorithm.HS512, jwtSecurityProperties.signingKey)
                .setClaims(getClaims(id, user, roles))
                .setSubject(id.toString())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(expiryTime))
                .compact()

        return Token(token, expiryTime)
    }

    @VisibleForTesting fun getClaims(id: UUID, user: UserSecurity, roles: MutableSet<String>): Map<String, Any> {
        val claims: MutableMap<String, Any> = HashMap()
        claims[ID] = id
        claims[USERNAME] = user.username
        claims[SEXE] = user.sexe
        claims[ROLES] = roles
        claims[ACCOUNT_NON_EXPIRED] = user.isAccountNonExpired
        claims[ACCOUNT_NON_LOCKED] = user.isAccountNonLocked
        claims[CREDENTIALS_NON_EXPIRED] = user.isCredentialsNonExpired
        claims[ENABLE] = user.isEnabled
        claims[TOKEN_CREATE_TIME] = Instant.now().truncatedTo(ChronoUnit.MINUTES).toString()
        return claims
    }
}