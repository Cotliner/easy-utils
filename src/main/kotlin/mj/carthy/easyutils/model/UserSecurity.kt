package mj.carthy.easyutils.model

import mj.carthy.easyutils.enums.Sex
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class UserSecurity(
  val id: UUID,
  val sex: Sex,
  private val username: String,
  private val password: String,
  private val authorities: MutableSet<out GrantedAuthority> = HashSet(),
  private val accountNonExpired: Boolean = false,
  private val accountNonLocked: Boolean = false,
  private val credentialsNonExpired: Boolean = false,
  private val enabled: Boolean = false
) : UserDetails {
    override fun getUsername(): String = username
    override fun getPassword(): String = password;
    override fun isAccountNonExpired(): Boolean = accountNonExpired
    override fun isAccountNonLocked(): Boolean = accountNonLocked
    override fun isCredentialsNonExpired(): Boolean = credentialsNonExpired
    override fun isEnabled(): Boolean = enabled
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities
}