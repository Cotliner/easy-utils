package mj.carthy.easyutils.helper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.springframework.util.ReflectionUtils

open class Errors {
    companion object {
        class ErrorCode constructor(val value: String)

        @JvmField val ENTITY_NOT_FOUND: ErrorCode = ErrorCode("ENTITY_NOT_FOUND")
        @JvmField val PROPERTY_NOT_FOUND: ErrorCode = ErrorCode("PROPERTY_NOT_FOUND")
        @JvmField val SERVER_ERROR: ErrorCode = ErrorCode("SERVER_ERROR")
        @JvmField val ZODIAC_SIGN_NOT_FOUND: ErrorCode = ErrorCode("ZODIAC_SIGN_NOT_FOUND")
    }

    open fun errors(): Flow<ErrorCode> = Errors::class.java.declaredFields.asFlow().filter {
        it.type == ErrorCode::class.java
    }.map { ReflectionUtils.getField(it, Errors::class.java) as ErrorCode }
}