package mj.carthy.profileservice.securities.scope

import org.springframework.security.access.prepost.PreAuthorize

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize(Scope.ADMIN_OR_ME)
annotation class AdminOrMe