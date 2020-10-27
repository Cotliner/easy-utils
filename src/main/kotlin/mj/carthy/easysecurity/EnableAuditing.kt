package mj.carthy.easysecurity

import mj.carthy.easysecurity.audit.AuditingConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Import(AuditingConfiguration::class)
annotation class EnableAuditing