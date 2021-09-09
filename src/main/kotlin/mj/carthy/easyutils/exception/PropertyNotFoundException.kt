package mj.carthy.easyutils.exception

import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND

data class PropertyNotFoundException(
    override val code: ErrorCode,
    override val message: String
): CustomException(
    code,
    message
) {
    override val httpCode: HttpStatus = NOT_FOUND
}