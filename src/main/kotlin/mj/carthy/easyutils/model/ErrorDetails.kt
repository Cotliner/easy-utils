package mj.carthy.easyutils.model

import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import org.springframework.http.HttpStatus
import java.time.Instant

data class ErrorDetails(
        val timestamp: Instant,
        val message: String,
        val path: String,
        val label: HttpStatus,
        val httpCode: Int,
        val code: ErrorCode,
        val fieldErrors: MutableSet<CustomFieldError> = HashSet()
)