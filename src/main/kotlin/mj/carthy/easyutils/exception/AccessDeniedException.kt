package mj.carthy.easyutils.exception

import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import org.springframework.http.HttpStatus

data class AccessDeniedException(override val var1: String, override val code: ErrorCode): CustomException(var1, code) {
    override fun httpCode(): HttpStatus = HttpStatus.UNAUTHORIZED
}