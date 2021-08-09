package mj.carthy.easyutils.configuration

import mj.carthy.easyutils.validator.PasswordConstraintValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration class ValidatorConfig {
    @Bean fun passwordConstraintValidator(): PasswordConstraintValidator = PasswordConstraintValidator()
}