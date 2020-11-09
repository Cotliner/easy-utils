package mj.carthy.easyutils.exception

import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND

data class PropertyNotFoundException(override val message: String, override val code: ErrorCode) : CustomException(message, code) {
    override fun httpCode(): HttpStatus = NOT_FOUND
}