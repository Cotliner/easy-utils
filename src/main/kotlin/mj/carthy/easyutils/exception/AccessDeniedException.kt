package mj.carthy.easyutils.exception

import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import org.springframework.http.HttpStatus

data class AccessDeniedException(override val message: String, override val code: ErrorCode): CustomException(message, code) {
    override fun httpCode(): HttpStatus = HttpStatus.UNAUTHORIZED
}