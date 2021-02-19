package mj.carthy.easyutils.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@ConditionalOnProperty(value = ["scheduler.enable"], havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
class SchedulerConfiguration