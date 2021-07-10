package mj.carthy.easyutils.exception

import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import org.springframework.http.HttpStatus

data class AccessDeniedException(
    override val code: ErrorCode,
    override val message: String
): CustomException(
    code,
    message
) {
    override fun httpCode(): HttpStatus = HttpStatus.UNAUTHORIZED
}