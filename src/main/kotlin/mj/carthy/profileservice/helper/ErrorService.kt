package mj.carthy.profileservice.helper

import mj.carthy.easyutils.helper.Errors
import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import org.springframework.util.ReflectionUtils
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.jvm.javaField
import kotlin.streams.toList

class ErrorService: Errors() {

    companion object {
        val AUTHENTICATION_DENIED: ErrorCode = ErrorCode("AUTHENTICATION_DENIED")
        val UNPROCESSABLE_ENTITY: ErrorCode = ErrorCode("UNPROCESSABLE_ENTITY")
        val EMAIL_VERIFICATION_FAILED: ErrorCode = ErrorCode("EMAIL_VERIFICATION_FAILED")
        val RESET_PASSWORD: ErrorCode = ErrorCode("RESET_PASSWORD")
        val USER_NOT_ENABLE: ErrorCode = ErrorCode("USER_NOT_ENABLE")
        val CONTACT_US: ErrorCode = ErrorCode("CONTACT_US")
        val CODE_VERIFICATION_FAILED: ErrorCode = ErrorCode("CODE_VERIFICATION_FAILED")
        val PASSWORD_CODE_VERIFICATION: ErrorCode = ErrorCode("PASSWORD_CODE_VERIFICATION")
        val USER_DUPLICATION: ErrorCode = ErrorCode("USER_DUPLICATION")
        val PROFILE_DUPLICATION: ErrorCode = ErrorCode("PROFILE_DUPLICATION")
        val INCORRECT_REASON: ErrorCode = ErrorCode("INCORRECT_REASON")
        val NUMBER_PICTURE_EXCEED: ErrorCode = ErrorCode("NUMBER_PICTURE_EXCEED")
        val NUMBER_PICTURE_PROFILE_EXCEED: ErrorCode = ErrorCode("NUMBER_PICTURE_PROFILE_EXCEED")
        val CURRENCY_NOT_FOUND: ErrorCode = ErrorCode("CURRENCY_NOT_FOUND")

        fun errors(): Array<ErrorCode> = Stream.of(ErrorService::javaClass.javaField).filter(
                Objects::nonNull
        ).map {
            field -> ReflectionUtils.getField(field!!, ErrorService::class.java)
        }.map { elem -> elem as ErrorCode }.toList().toTypedArray()
    }
}