package mj.carthy.easyutils.exception

import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import org.springframework.http.HttpStatus

abstract class CustomException constructor(open val code: ErrorCode, override val message: String) : RuntimeException() {
    abstract fun httpCode(): HttpStatus
}