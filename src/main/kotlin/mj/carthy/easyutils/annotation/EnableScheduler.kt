package mj.carthy.easyutils.annotation

import mj.carthy.easyutils.configuration.SchedulerConfiguration
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Import(SchedulerConfiguration::class)
annotation class EnableScheduler()
