package mj.carthy.easyutils.helper

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import mj.carthy.easyutils.BaseUnitTest
import mj.carthy.easyutils.document.BaseDocument
import mj.carthy.easyutils.exception.EntityNotFoundException
import mj.carthy.easyutils.exception.UnprocessedException
import mj.carthy.easyutils.helper.Errors.Companion.ENTITY_NOT_FOUND
import mj.carthy.easyutils.helper.Errors.Companion.VALIDATION_ERROR
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import reactor.core.publisher.Mono
import java.time.Instant.now
import java.time.LocalDate
import java.util.*
import kotlin.Exception
import kotlin.reflect.KSuspendFunction1

internal class ToolsTest: BaseUnitTest() {

  @Test fun `should format date time with pattern ddMMYY`() {
    /* GIVEN */
    val expected = "08/11/1991"
    val date: LocalDate = LocalDate.of(1991, 11, 8)
    /* WHEN */
    val result: String = date.format()
    /* THEN */
    result shouldBe expected
  }

  @Test fun `should create error with exception`() {
    /* GIVEN */
    val requestPath = "path"
    val errorMessage = "message"
    val exception: Exception = Exception(errorMessage)
    val request: MockServerHttpRequest = MockServerHttpRequest.get(requestPath).build()
    /* WHEN */
    val (timestamp, message, path, label, httpCode, code, fieldErrors) = error(exception, request, BAD_REQUEST, VALIDATION_ERROR)
    /* THEN */
    timestamp shouldBeBefore now()
    message shouldBe "Exception: $errorMessage"
    path shouldBe requestPath
    label shouldBe BAD_REQUEST
    httpCode shouldBe BAD_REQUEST.value()
    code shouldBe VALIDATION_ERROR
    fieldErrors.shouldBeEmpty()
  }

  @Test fun `should create error with message`() {
    /* GIVEN */
    val requestPath = "path"
    val errorMessage = "message"
    val request: MockServerHttpRequest = MockServerHttpRequest.get(requestPath).build()
    /* WHEN */
    val (timestamp, message, path, label, httpCode, code, fieldErrors) = error(errorMessage, request, BAD_REQUEST, VALIDATION_ERROR)
    /* THEN */
    timestamp shouldBeBefore now()
    message shouldBe errorMessage
    path shouldBe requestPath
    label shouldBe BAD_REQUEST
    httpCode shouldBe BAD_REQUEST.value()
    code shouldBe VALIDATION_ERROR
    fieldErrors.shouldBeEmpty()
  }

  @Test fun `should create if object is null`() {
    /* GIVEN */
    val document: BaseDocument<UUID>? = null
    /* WHEN */
    val result: BaseDocument<UUID> = retrieveOrNew(document)
    /* THEN */
    result.shouldNotBeNull()
  }

  @Test fun `should find when request return object`() {
    /* GIVEN */
    val request: (UUID) -> Optional<BaseDocument<UUID>> = { uuid -> Optional.of(BaseDocument(uuid)) }
    val id: UUID = "3cb83438-ba27-4b04-8c1c-b833704eeeb4".uuid
    /* WHEN */
    val result: BaseDocument<UUID> = findOrThrow(request, id)
    /* THEN */
    result.id shouldBe id
  }

  @Test fun `should not find and throw EntityNotFoundException`() {
    /* GIVEN */
    val request: (UUID) -> Optional<BaseDocument<UUID>> = { _ -> Optional.empty() }
    val id: UUID = "3cb83438-ba27-4b04-8c1c-b833704eeeb4".uuid
    /* WHEN */
    val (code, message) = shouldThrow<EntityNotFoundException> { findOrThrow(request, id) }
    /* THEN */
    code shouldBe ENTITY_NOT_FOUND
    message shouldBe "Can not found BaseDocument with id : $id"
  }

  @Test fun `should return single when request return object`() {
    /* GIVEN */
    val request: (UUID) -> Mono<BaseDocument<UUID>> = { uuid -> BaseDocument(uuid).mono }
    val id: UUID = "3cb83438-ba27-4b04-8c1c-b833704eeeb4".uuid
    /* WHEN */
    val result: BaseDocument<UUID> = runBlocking { singleOrError(request, id) }
    /* THEN */
    result.id shouldBe id
  }

  @Test fun `should not return single and throw EntityNotFoundException when request return nothing`() {
    /* GIVEN */
    val request: (UUID) -> Mono<BaseDocument<UUID>> = { _ -> Mono.empty() }
    val id: UUID = "3cb83438-ba27-4b04-8c1c-b833704eeeb4".uuid
    /* WHEN */
    val (code, message) = shouldThrow<EntityNotFoundException> { runBlocking { singleOrError(request, id) } }
    /* THEN */
    code shouldBe ENTITY_NOT_FOUND
    message shouldBe "Can not found BaseDocument with id : $id"
  }

  @Test fun `should return single when suspend request return object`() {
    /* GIVEN */
    val request: suspend (UUID) -> BaseDocument<UUID>? = { uuid -> BaseDocument(uuid) }
    val id: UUID = "3cb83438-ba27-4b04-8c1c-b833704eeeb4".uuid
    /* WHEN */
    val result: BaseDocument<UUID> = runBlocking { singleOrError(request, id) }
    /* THEN */
    result.id shouldBe id
  }

  @Test fun `should not return suspend single and throw EntityNotFoundException when request return nothing`() {
    /* GIVEN */
    val request: suspend (UUID) -> BaseDocument<UUID>? = { _ -> null }
    val id: UUID = "3cb83438-ba27-4b04-8c1c-b833704eeeb4".uuid
    /* WHEN */
    val (code, message) = shouldThrow<EntityNotFoundException> { runBlocking { singleOrError(request, id) } }
    /* THEN */
    code shouldBe ENTITY_NOT_FOUND
    message shouldBe "Can not found BaseDocument with id : $id"
  }

  @Test fun `should throw EntityNotFoundException`() {
    /* GIVEN */
    val id: UUID = "3cb83438-ba27-4b04-8c1c-b833704eeeb4".uuid
    /* WHEN */
    val (code, message) = entityNotFoundException<UUID, BaseDocument<UUID>>(id)
    /* THEN */
    code shouldBe ENTITY_NOT_FOUND
    message shouldBe "Can not found BaseDocument with id : $id"
  }
}