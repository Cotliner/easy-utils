package mj.carthy.profileservice.entities.user

import java.time.Instant
import javax.validation.Valid

data class Connection(
        val isValidPassword: Boolean,
        val date: Instant,
        @Valid val local: Local,
        @Valid val system: System,
        @Valid val browser: WebBrowser
)