package mj.carthy.easysecurity.jwtconfiguration

import org.apache.commons.lang3.math.NumberUtils.LONG_ONE
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.temporal.ChronoUnit

@ConfigurationProperties(prefix = "security.jwt")
data class JwtSecurityProperties(
        val signingKey: String = "g~DS<EHd)Vr+C&#8:[ba",
        val validity: Long = LONG_ONE,
        val unit: ChronoUnit = ChronoUnit.DAYS
)