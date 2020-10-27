package mj.carthy.easysecurity

import mj.carthy.easysecurity.configuration.DefaultServiceSecurityConfiguration
import mj.carthy.easysecurity.jwtconfiguration.JwtTokenConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Import(JwtTokenConfiguration::class, DefaultServiceSecurityConfiguration::class)
annotation class EnableDefaultSecurity