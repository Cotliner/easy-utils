package mj.carthy.easyutils.helper

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field

open class Errors {
    companion object {
        class ErrorCode constructor(val value: String)

        @JvmField val ENTITY_NOT_FOUND: ErrorCode = ErrorCode("ENTITY_NOT_FOUND")
        @JvmField val PROPERTY_NOT_FOUND: ErrorCode = ErrorCode("PROPERTY_NOT_FOUND")
        @JvmField val SERVER_ERROR: ErrorCode = ErrorCode("SERVER_ERROR")
        @JvmField val VALIDATION_ERROR: ErrorCode = ErrorCode("VALIDATION_ERROR")
        @JvmField val AUTHENTICATION_DENIED: ErrorCode = ErrorCode("AUTHENTICATION_DENIED")
        @JvmField val ZODIAC_SIGN_NOT_FOUND: ErrorCode = ErrorCode("ZODIAC_SIGN_NOT_FOUND")
    }

    @ExperimentalCoroutinesApi val errors get(): Flow<ErrorCode> = merge(
        Errors::class.java.declaredFields.asFlow(),
        this::class.java.declaredFields.asFlow()
    ).filter {
        it is Field && it.type == ErrorCode::class.java
    }.map { ReflectionUtils.getField(it as Field, this::class.java) as ErrorCode }
}