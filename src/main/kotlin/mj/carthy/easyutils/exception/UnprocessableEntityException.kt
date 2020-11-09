package mj.carthy.easyutils.exception

import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

data class UnprocessableEntityException(override val message: String, override val code: ErrorCode): CustomException(message, code) {
    override fun httpCode(): HttpStatus = UNPROCESSABLE_ENTITY
}