package mj.carthy.easyutils

import mj.carthy.easyutils.configuration.PasswordValidatorConfig
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Import(PasswordValidatorConfig::class)
annotation class EnablePasswordValidation