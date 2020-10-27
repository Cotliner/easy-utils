package mj.carthy.profileservice.securities.scope.editor

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import mj.carthy.profileservice.repositories.reactive.UserReactiveRepository
import mj.carthy.profileservice.repositories.simple.UserRepository
import org.springframework.stereotype.Component
import java.beans.PropertyEditorSupport
import java.util.*

@Component class UserEditor(val userRepository: UserRepository) : PropertyEditorSupport() {
    override fun setAsText(id: String) { value = userRepository.findById(UUID.fromString(id)).orElse(null) }
}