package mj.carthy.easyutils.helper

import org.springframework.util.ReflectionUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.Flux.merge
import reactor.kotlin.core.publisher.toFlux
import java.lang.reflect.Field

open class Errors {
    companion object {
        class ErrorCode constructor(val value: String)

        @JvmField val ENTITY_NOT_FOUND: ErrorCode = ErrorCode("ENTITY_NOT_FOUND")
        @JvmField val PROPERTY_NOT_FOUND: ErrorCode = ErrorCode("PROPERTY_NOT_FOUND")
        @JvmField val SERVER_ERROR: ErrorCode = ErrorCode("SERVER_ERROR")
        @JvmField val ZODIAC_SIGN_NOT_FOUND: ErrorCode = ErrorCode("ZODIAC_SIGN_NOT_FOUND")
    }

    fun errors(): Flux<ErrorCode> = merge(
        Errors::class.java.declaredFields.toFlux(),
        this::class.java.declaredFields.toFlux()
    ).filter {
        it is Field && it.type == ErrorCode::class.java
    }.map { ReflectionUtils.getField(it as Field, this::class.java) as ErrorCode }
}