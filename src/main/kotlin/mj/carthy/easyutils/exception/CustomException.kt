package mj.carthy.easyutils.exception

import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import org.springframework.http.HttpStatus

abstract class CustomException constructor(override val message: String, open val code: ErrorCode) : RuntimeException() {
    abstract fun httpCode(): HttpStatus
}