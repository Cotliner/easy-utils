package mj.carthy.easyutils.helper

import org.springframework.util.ReflectionUtils
import java.lang.reflect.Field
import java.util.*
import kotlin.streams.toList

open class Errors {


    companion object {
        class ErrorCode constructor(val value: String)

        val ENTITY_NOT_FOUND: ErrorCode = ErrorCode("ENTITY_NOT_FOUND")
        val PROPERTY_NOT_FOUND: ErrorCode = ErrorCode("PROPERTY_NOT_FOUND")
        val SERVER_ERROR: ErrorCode = ErrorCode("SERVER_ERROR")
        val ZODIC_SIGN_NOT_FOUND: ErrorCode = ErrorCode("ZODIC_SIGN_NOT_FOUND")
    }

    fun errors(): Array<ErrorCode> = Arrays.stream<Field>(ErrorCode::class.java.fields).map {
            field: Field -> ReflectionUtils.getField(
            field,
            Errors::class.java
    ) }.map{ it as ErrorCode }.toList().toTypedArray()
}