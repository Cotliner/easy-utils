package mj.carthy.easyutils.model

import java.time.Instant

data class Token(val value: String, val expiryTime: Instant)