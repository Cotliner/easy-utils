package mj.carthy.easyutils

import mj.carthy.easyutils.configuration.ValidatorConfig
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Import(ValidatorConfig::class)
annotation class EnableValidation