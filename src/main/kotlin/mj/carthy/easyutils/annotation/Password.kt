package mj.carthy.easyutils.annotation

import mj.carthy.easyutils.validator.PasswordConstraintValidator
import javax.validation.Constraint
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [PasswordConstraintValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Password(val message: String = "Invalid Password", val groups: Array<KClass<*>> = [], val payload: Array<KClass<*>> = [])