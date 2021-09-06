package mj.carthy.easyutils.helper

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.runBlocking
import mj.carthy.easyutils.BaseUnitTest
import mj.carthy.easyutils.document.BaseDocument
import mj.carthy.easyutils.enums.ZodiacSign
import mj.carthy.easyutils.enums.ZodiacSign.*
import mj.carthy.easyutils.exception.EntityNotFoundException
import mj.carthy.easyutils.helper.Errors.Companion.ENTITY_NOT_FOUND
import mj.carthy.easyutils.helper.Errors.Companion.VALIDATION_ERROR
import mj.carthy.easyutils.model.PaginationResult
import org.apache.commons.lang3.math.NumberUtils.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.MINUTES
import java.util.*
import java.util.stream.Stream
import kotlin.Exception
import kotlin.time.ExperimentalTime

@TestInstance(PER_CLASS) internal class ToolsTest: BaseUnitTest() {

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
    timestamp shouldBeBefore Instant.now()
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
    timestamp shouldBeBefore Instant.now()
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

  @Test fun `should get set from flux`() {
    /* GIVEN */
    val document = BaseDocument("35d0be0d-4125-471d-a86a-9896c758effe".uuid)
    /* WHEN */
    val result: Set<BaseDocument<UUID>> = runBlocking { Flux.just(document, document).toSet() }
    /* THEN */
    result shouldContainExactlyInAnyOrder setOf(document)
  }

  @Test fun `should convert without known class`() {
    /* GIVEN */
    val mapper = ObjectMapper()
    /* WHEN */
    val result: BaseDocument<UUID> = mapper.convert("{\"id\":\"76751f68-cf97-43c2-a3cc-3f6867baa9e2\"}")
    /* THEN */
    result.id shouldBe "76751f68-cf97-43c2-a3cc-3f6867baa9e2"
  }

  @MethodSource("shouldReturnTrueOrFalseWhenLocalDateIsBetweenOrNot")
  @ParameterizedTest fun `should return TRUE or FALSE when LocalDate is between or not`(
    /* GIVEN */
    before: LocalDate,
    after: LocalDate,
    date: LocalDate,
    expected: Boolean
  ) {
    /* WHEN */
    val result: Boolean = date.isBetween(before, after)
    /* THEN */
    result shouldBe expected
  }

  private fun shouldReturnTrueOrFalseWhenLocalDateIsBetweenOrNot(): Stream<Arguments> = Stream.of(
    Arguments.of(
      LocalDate.of(1991, 10, 8),
      LocalDate.of(1991, 12, 8),
      LocalDate.of(1991, 11, 8),
      TRUE
    ),
    Arguments.of(
      LocalDate.of(1991, 11, 8),
      LocalDate.of(1991, 12, 8),
      LocalDate.of(1991, 11, 8),
      TRUE
    ),
    Arguments.of(
      LocalDate.of(1991, 10, 8),
      LocalDate.of(1991, 11, 8),
      LocalDate.of(1991, 11, 8),
      TRUE
    ),
    Arguments.of(
      LocalDate.of(1991, 12, 8),
      LocalDate.of(1991, 10, 8),
      LocalDate.of(1991, 11, 8),
      FALSE
    ),
  )

  @MethodSource("shouldReturnTrueOrFalseWhenInstantIsBetweenOrNot")
  @ParameterizedTest fun `should return TRUE or FALSE when Instant is between or not`(
    /* GIVEN */
    before: Instant,
    after: Instant,
    date: Instant,
    expected: Boolean
  ) {
    /* WHEN */
    val result: Boolean = date.isBetween(before, after)
    /* THEN */
    result shouldBe expected
  }

  private fun shouldReturnTrueOrFalseWhenInstantIsBetweenOrNot(): Stream<Arguments> = Stream.of(
    Arguments.of(
      Instant.parse("1991-10-08T00:00:00.00Z"),
      Instant.parse("1991-12-08T00:00:00.00Z"),
      Instant.parse("1991-11-08T00:00:00.00Z"),
      TRUE
    ),
    Arguments.of(
      Instant.parse("1991-11-08T00:00:00.00Z"),
      Instant.parse("1991-12-08T00:00:00.00Z"),
      Instant.parse("1991-11-08T00:00:00.00Z"),
      TRUE
    ),
    Arguments.of(
      Instant.parse("1991-10-08T00:00:00.00Z"),
      Instant.parse("1991-11-08T00:00:00.00Z"),
      Instant.parse("1991-11-08T00:00:00.00Z"),
      TRUE
    ),
    Arguments.of(
      Instant.parse("1991-12-08T00:00:00.00Z"),
      Instant.parse("1991-10-08T00:00:00.00Z"),
      Instant.parse("1991-11-08T00:00:00.00Z"),
      FALSE
    ),
  )

  @Test fun `should convert LocalDate to Instant`() {
    /* GIVEN */
    val date: LocalDate = LocalDate.of(1991, 11, 8)
    /* WHEN */
    val result: Instant = date.instant
    /* THEN */
    result.string shouldStartWith  "1991-11-0"
  }

  @Test fun `should convert Instant to LocalDate`() {
    /* GIVEN */
    val date = Instant.parse("1991-11-08T00:00:00.00Z")
    /* WHEN */
    val result = date.localDate
    /* THEN */
    result.year shouldBe 1991
    result.monthValue shouldBe 11
    result.dayOfMonth shouldBe 8
  }

  @Test fun `should format number`() {
    /* GIVEN */
    val number: Number = 8.54038420
    /* WHEN */
    val result = number.format()
    /* THEN */
    result shouldBe "8.54"
  }

  @Test fun `should not throw IllegalArgumentException when path is empty`() {
    /* GIVEN */
    val exchange: AuthorizeExchangeSpec = ServerHttpSecurity.http().authorizeExchange()

    /* THEN */shouldNotThrow<IllegalArgumentException> { /* WHEN */ exchange.emptyAblePathMatchers() }
  }

  @Test fun `should add path when is not empty`() {
    /* GIVEN */
    val exchange: AuthorizeExchangeSpec = ServerHttpSecurity.http().authorizeExchange()
    /* WHEN && THEN */
    exchange.emptyAblePathMatchers("path")
  }

  @Test fun `should create pagination result`() {
    /* GIVEN */
    val docuOne = BaseDocument("b6d290ab-4ccd-444d-812b-433542d0ac3b".uuid)
    val docuTwo = BaseDocument("651e0c0f-172f-43ca-80fe-25759ef32b7b".uuid)
    val documents = setOf(docuOne, docuTwo)
    /* WHEN */
    val result: PaginationResult<BaseDocument<UUID>> = documents.paginationResult(0, 1)
    /* THEN */
    result.page shouldBe 0
    result.size shouldBe 1
    result.content shouldContainExactly documents
  }

  @Test fun `should return TRUE when Instant is more than eighteen years`() {
    /* GIVEN */
    val date: Instant = Instant.parse("1991-11-08T00:00:00.00Z")
    /* WHEN */
    val result: Boolean = date.moreThanEighteen
    /* THEN */
    result shouldBe TRUE
  }

  @Test fun `should return FALSE when Instant is not more than eighteen years`() {
    /* GIVEN */
    val date: Instant = Instant.now()
    /* WHEN */
    val result: Boolean = date.moreThanEighteen
    /* THEN */
    result shouldBe FALSE
  }

  @Test fun `should return TRUE when LocalDate is more than eighteen years`() {
    /* GIVEN */
    val date: LocalDate = LocalDate.of(1991, 11, 8)
    /* WHEN */
    val result: Boolean = date.moreThanEighteen
    /* THEN */
    result shouldBe TRUE
  }

  @Test fun `should return FALSE when LocalDate is not more than eighteen years`() {
    /* GIVEN */
    val date: LocalDate = LocalDate.now()
    /* WHEN */
    val result: Boolean = date.moreThanEighteen
    /* THEN */
    result shouldBe FALSE
  }

  @Test fun `should return TRUE if BigDecimal is positive`() {
    /* GIVEN */
    val number = "3.3".toBigDecimal()
    /* WHEN */
    val result: Boolean = number.isPositive
    /* THEN */
    result shouldBe TRUE
  }

  @Test fun `should return FALSE if BigDecimal is negative`() {
    /* GIVEN */
    val number = "-3.3".toBigDecimal()
    /* WHEN */
    val result: Boolean = number.isPositive
    /* THEN */
    result shouldBe FALSE
  }

  @Test fun `should found the max value`() {
    /* GIVEN */
    val map = mapOf("one" to 1, "two" to 2, "three" to 3, "four" to 4)
    /* WHEN */
    val result: Int? = map.maxByValue
    /* THEN */
    result shouldBe 4
  }

  @Test fun `should found the max key by value`() {
    /* GIVEN */
    val map = mapOf(1 to "one", 2 to "two", 3 to "three", 4 to "four")
    /* WHEN */
    val result: String? = map.maxByKey
    /* THEN */
    result shouldBe "four"
  }

  @Test fun `should give string`() {
    /* GIVEN */
    val one: Number = 1
    /* WHEN */
    val result: String = one.string
    /* THEN */
    result shouldBe "1"
  }

  @Test fun `should change string in uuid`() {
    /* GIVEN */
    val string = "b6cfe15f-79f6-4374-92b3-91da50b52e58"
    /* WHEN*/
    val result: UUID = string.uuid
    /* THEN */
    result shouldBe UUID.fromString(string)
  }

  @ExperimentalTime
  @Test fun `should return TRUE when duration is positive`() {
    /* GIVEN */
    val duration = Duration.of(1, MINUTES)
    /* WHEN */
    val result: Boolean = duration.isPositive
    /* THEN */
    result shouldBe TRUE
  }

  @ExperimentalTime
  @Test fun `should return FALSE when duration is negative`() {
    /* GIVEN */
    val duration = Duration.of(-1, MINUTES)
    /* WHEN */
    val result: Boolean = duration.isPositive
    /* THEN */
    result shouldBe FALSE
  }

  @Test fun `should return TRUE when Instant after now`() {
    /* GIVEN */
    val date: Instant = Instant.now().plus(LONG_ONE, DAYS)
    /* WHEN */
    val result: Boolean = date.isPositive
    /* THEN */
    result shouldBe TRUE
  }

  @Test fun `should return FALSE when Instant before now`() {
    /* GIVEN */
    val date: Instant = Instant.now().plus(LONG_MINUS_ONE, DAYS)
    /* WHEN */
    val result: Boolean = date.isPositive
    /* THEN */
    result shouldBe FALSE
  }

  @Test fun `should return FALSE when Instant after now`() {
    /* GIVEN */
    val date: Instant = Instant.now().plus(LONG_ONE, DAYS)
    /* WHEN */
    val result: Boolean = date.isNegative
    /* THEN */
    result shouldBe FALSE
  }

  @Test fun `should return TRUE when Instant before now`() {
    /* GIVEN */
    val date: Instant = Instant.now().plus(LONG_MINUS_ONE, DAYS)
    /* WHEN */
    val result: Boolean = date.isNegative
    /* THEN */
    result shouldBe TRUE
  }

  @Test fun `should collect by id from flux`() {
    /* GIVEN */
    val idOne: UUID = "b6d290ab-4ccd-444d-812b-433542d0ac3b".uuid
    val idTwo: UUID = "651e0c0f-172f-43ca-80fe-25759ef32b7b".uuid
    val docuOne = BaseDocument(idOne)
    val docuTwo = BaseDocument(idTwo)
    val documents: Flux<BaseDocument<UUID>> = Flux.just(docuOne, docuTwo)
    /* WHEN */
    val result: Map<UUID?, BaseDocument<UUID>> = runBlocking { documents.collectById.awaitSingle() }
    /* THEN */
    result.keys shouldContainExactlyInAnyOrder listOf(idOne, idTwo)
  }

  @Test fun `should collect by id from collection`() {
    /* GIVEN */
    val idOne: UUID = "b6d290ab-4ccd-444d-812b-433542d0ac3b".uuid
    val idTwo: UUID = "651e0c0f-172f-43ca-80fe-25759ef32b7b".uuid
    val docuOne = BaseDocument(idOne)
    val docuTwo = BaseDocument(idTwo)
    val documents: Collection<BaseDocument<UUID>> = listOf(docuOne, docuTwo)
    /* WHEN */
    val result: Map<UUID?, BaseDocument<UUID>> = documents.collectById
    /* THEN */
    result.keys shouldContainExactlyInAnyOrder listOf(idOne, idTwo)
  }

  @MethodSource("shouldFoundZodiacSign")
  @ParameterizedTest fun `should found zodiac sign`(
    /* GIVEN */
    date: LocalDate,
    expected: ZodiacSign
  ) {
    /* WHEN */
    val result: ZodiacSign = date.zodiacSign
    /* THEN */
    result shouldBe expected
  }

  private fun shouldFoundZodiacSign(): Stream<Arguments> = Stream.of(
    /* TEST CAPRICORN */
    Arguments.of(LocalDate.of(0, 1, 1), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 2), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 3), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 4), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 5), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 6), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 7), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 8), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 9), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 10), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 11), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 12), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 13), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 14), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 15), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 16), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 17), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 18), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 19), CAPRICORN),
    Arguments.of(LocalDate.of(0, 1, 20), CAPRICORN),
    /* TEST AQUARIUS */
    Arguments.of(LocalDate.of(0, 1, 21), AQUARIUS),
    Arguments.of(LocalDate.of(0, 1, 22), AQUARIUS),
    Arguments.of(LocalDate.of(0, 1, 23), AQUARIUS),
    Arguments.of(LocalDate.of(0, 1, 24), AQUARIUS),
    Arguments.of(LocalDate.of(0, 1, 25), AQUARIUS),
    Arguments.of(LocalDate.of(0, 1, 26), AQUARIUS),
    Arguments.of(LocalDate.of(0, 1, 27), AQUARIUS),
    Arguments.of(LocalDate.of(0, 1, 28), AQUARIUS),
    Arguments.of(LocalDate.of(0, 1, 29), AQUARIUS),
    Arguments.of(LocalDate.of(0, 1, 30), AQUARIUS),
    Arguments.of(LocalDate.of(0, 1, 31), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 1), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 2), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 3), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 4), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 5), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 6), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 7), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 8), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 9), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 10), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 11), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 12), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 13), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 14), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 15), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 16), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 17), AQUARIUS),
    Arguments.of(LocalDate.of(0, 2, 18), AQUARIUS),
    /* TEST PISCES */
    Arguments.of(LocalDate.of(0, 2, 19), PISCES),
    Arguments.of(LocalDate.of(0, 2, 20), PISCES),
    Arguments.of(LocalDate.of(0, 2, 21), PISCES),
    Arguments.of(LocalDate.of(0, 2, 22), PISCES),
    Arguments.of(LocalDate.of(0, 2, 23), PISCES),
    Arguments.of(LocalDate.of(0, 2, 24), PISCES),
    Arguments.of(LocalDate.of(0, 2, 25), PISCES),
    Arguments.of(LocalDate.of(0, 2, 26), PISCES),
    Arguments.of(LocalDate.of(0, 2, 27), PISCES),
    Arguments.of(LocalDate.of(0, 2, 28), PISCES),
    Arguments.of(LocalDate.of(0, 2, 29), PISCES),
    Arguments.of(LocalDate.of(0, 3, 1), PISCES),
    Arguments.of(LocalDate.of(0, 3, 2), PISCES),
    Arguments.of(LocalDate.of(0, 3, 3), PISCES),
    Arguments.of(LocalDate.of(0, 3, 4), PISCES),
    Arguments.of(LocalDate.of(0, 3, 5), PISCES),
    Arguments.of(LocalDate.of(0, 3, 6), PISCES),
    Arguments.of(LocalDate.of(0, 3, 7), PISCES),
    Arguments.of(LocalDate.of(0, 3, 8), PISCES),
    Arguments.of(LocalDate.of(0, 3, 9), PISCES),
    Arguments.of(LocalDate.of(0, 3, 10), PISCES),
    Arguments.of(LocalDate.of(0, 3, 11), PISCES),
    Arguments.of(LocalDate.of(0, 3, 12), PISCES),
    Arguments.of(LocalDate.of(0, 3, 13), PISCES),
    Arguments.of(LocalDate.of(0, 3, 14), PISCES),
    Arguments.of(LocalDate.of(0, 3, 15), PISCES),
    Arguments.of(LocalDate.of(0, 3, 16), PISCES),
    Arguments.of(LocalDate.of(0, 3, 17), PISCES),
    Arguments.of(LocalDate.of(0, 3, 18), PISCES),
    Arguments.of(LocalDate.of(0, 3, 19), PISCES),
    Arguments.of(LocalDate.of(0, 3, 20), PISCES),
    Arguments.of(LocalDate.of(0, 3, 21), PISCES),
    /* TEST ARIES */
    Arguments.of(LocalDate.of(0, 3, 22), ARIES),
    Arguments.of(LocalDate.of(0, 3, 23), ARIES),
    Arguments.of(LocalDate.of(0, 3, 24), ARIES),
    Arguments.of(LocalDate.of(0, 3, 25), ARIES),
    Arguments.of(LocalDate.of(0, 3, 26), ARIES),
    Arguments.of(LocalDate.of(0, 3, 27), ARIES),
    Arguments.of(LocalDate.of(0, 3, 28), ARIES),
    Arguments.of(LocalDate.of(0, 3, 29), ARIES),
    Arguments.of(LocalDate.of(0, 3, 30), ARIES),
    Arguments.of(LocalDate.of(0, 3, 31), ARIES),
    Arguments.of(LocalDate.of(0, 4, 1), ARIES),
    Arguments.of(LocalDate.of(0, 4, 2), ARIES),
    Arguments.of(LocalDate.of(0, 4, 3), ARIES),
    Arguments.of(LocalDate.of(0, 4, 4), ARIES),
    Arguments.of(LocalDate.of(0, 4, 5), ARIES),
    Arguments.of(LocalDate.of(0, 4, 6), ARIES),
    Arguments.of(LocalDate.of(0, 4, 7), ARIES),
    Arguments.of(LocalDate.of(0, 4, 8), ARIES),
    Arguments.of(LocalDate.of(0, 4, 9), ARIES),
    Arguments.of(LocalDate.of(0, 4, 10), ARIES),
    Arguments.of(LocalDate.of(0, 4, 11), ARIES),
    Arguments.of(LocalDate.of(0, 4, 12), ARIES),
    Arguments.of(LocalDate.of(0, 4, 13), ARIES),
    Arguments.of(LocalDate.of(0, 4, 14), ARIES),
    Arguments.of(LocalDate.of(0, 4, 15), ARIES),
    Arguments.of(LocalDate.of(0, 4, 16), ARIES),
    Arguments.of(LocalDate.of(0, 4, 17), ARIES),
    Arguments.of(LocalDate.of(0, 4, 18), ARIES),
    Arguments.of(LocalDate.of(0, 4, 19), ARIES),
    Arguments.of(LocalDate.of(0, 4, 20), ARIES),
    /* TEST TAURUS*/
    Arguments.of(LocalDate.of(0, 4, 21), TAURUS),
    Arguments.of(LocalDate.of(0, 4, 22), TAURUS),
    Arguments.of(LocalDate.of(0, 4, 23), TAURUS),
    Arguments.of(LocalDate.of(0, 4, 24), TAURUS),
    Arguments.of(LocalDate.of(0, 4, 25), TAURUS),
    Arguments.of(LocalDate.of(0, 4, 26), TAURUS),
    Arguments.of(LocalDate.of(0, 4, 27), TAURUS),
    Arguments.of(LocalDate.of(0, 4, 28), TAURUS),
    Arguments.of(LocalDate.of(0, 4, 29), TAURUS),
    Arguments.of(LocalDate.of(0, 4, 30), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 1), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 2), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 3), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 4), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 5), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 6), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 7), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 8), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 9), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 10), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 11), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 12), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 13), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 14), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 15), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 16), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 17), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 18), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 19), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 20), TAURUS),
    Arguments.of(LocalDate.of(0, 5, 21), TAURUS),
    /* TEST GEMINI */
    Arguments.of(LocalDate.of(0, 6, 1), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 2), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 3), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 4), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 5), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 6), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 7), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 8), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 9), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 10), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 11), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 12), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 13), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 14), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 15), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 16), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 17), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 18), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 19), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 20), GEMINI),
    Arguments.of(LocalDate.of(0, 6, 21), GEMINI),
    /* TEST CANCER*/
    Arguments.of(LocalDate.of(0, 6, 22), CANCER),
    Arguments.of(LocalDate.of(0, 6, 23), CANCER),
    Arguments.of(LocalDate.of(0, 6, 24), CANCER),
    Arguments.of(LocalDate.of(0, 6, 25), CANCER),
    Arguments.of(LocalDate.of(0, 6, 26), CANCER),
    Arguments.of(LocalDate.of(0, 6, 27), CANCER),
    Arguments.of(LocalDate.of(0, 6, 28), CANCER),
    Arguments.of(LocalDate.of(0, 6, 29), CANCER),
    Arguments.of(LocalDate.of(0, 6, 30), CANCER),
    Arguments.of(LocalDate.of(0, 7, 1), CANCER),
    Arguments.of(LocalDate.of(0, 7, 2), CANCER),
    Arguments.of(LocalDate.of(0, 7, 3), CANCER),
    Arguments.of(LocalDate.of(0, 7, 4), CANCER),
    Arguments.of(LocalDate.of(0, 7, 5), CANCER),
    Arguments.of(LocalDate.of(0, 7, 6), CANCER),
    Arguments.of(LocalDate.of(0, 7, 7), CANCER),
    Arguments.of(LocalDate.of(0, 7, 8), CANCER),
    Arguments.of(LocalDate.of(0, 7, 9), CANCER),
    Arguments.of(LocalDate.of(0, 7, 10), CANCER),
    Arguments.of(LocalDate.of(0, 7, 11), CANCER),
    Arguments.of(LocalDate.of(0, 7, 12), CANCER),
    Arguments.of(LocalDate.of(0, 7, 13), CANCER),
    Arguments.of(LocalDate.of(0, 7, 14), CANCER),
    Arguments.of(LocalDate.of(0, 7, 15), CANCER),
    Arguments.of(LocalDate.of(0, 7, 16), CANCER),
    Arguments.of(LocalDate.of(0, 7, 17), CANCER),
    Arguments.of(LocalDate.of(0, 7, 18), CANCER),
    Arguments.of(LocalDate.of(0, 7, 19), CANCER),
    Arguments.of(LocalDate.of(0, 7, 20), CANCER),
    Arguments.of(LocalDate.of(0, 7, 21), CANCER),
    Arguments.of(LocalDate.of(0, 7, 22), CANCER),
    Arguments.of(LocalDate.of(0, 7, 23), CANCER),
    /* TEST LEO */
    Arguments.of(LocalDate.of(0, 7, 24), LEO),
    Arguments.of(LocalDate.of(0, 7, 25), LEO),
    Arguments.of(LocalDate.of(0, 7, 26), LEO),
    Arguments.of(LocalDate.of(0, 7, 27), LEO),
    Arguments.of(LocalDate.of(0, 7, 28), LEO),
    Arguments.of(LocalDate.of(0, 7, 29), LEO),
    Arguments.of(LocalDate.of(0, 7, 30), LEO),
    Arguments.of(LocalDate.of(0, 8, 1), LEO),
    Arguments.of(LocalDate.of(0, 8, 2), LEO),
    Arguments.of(LocalDate.of(0, 8, 3), LEO),
    Arguments.of(LocalDate.of(0, 8, 4), LEO),
    Arguments.of(LocalDate.of(0, 8, 5), LEO),
    Arguments.of(LocalDate.of(0, 8, 6), LEO),
    Arguments.of(LocalDate.of(0, 8, 7), LEO),
    Arguments.of(LocalDate.of(0, 8, 8), LEO),
    Arguments.of(LocalDate.of(0, 8, 9), LEO),
    Arguments.of(LocalDate.of(0, 8, 10), LEO),
    Arguments.of(LocalDate.of(0, 8, 11), LEO),
    Arguments.of(LocalDate.of(0, 8, 12), LEO),
    Arguments.of(LocalDate.of(0, 8, 13), LEO),
    Arguments.of(LocalDate.of(0, 8, 14), LEO),
    Arguments.of(LocalDate.of(0, 8, 15), LEO),
    Arguments.of(LocalDate.of(0, 8, 16), LEO),
    Arguments.of(LocalDate.of(0, 8, 17), LEO),
    Arguments.of(LocalDate.of(0, 8, 18), LEO),
    Arguments.of(LocalDate.of(0, 8, 19), LEO),
    Arguments.of(LocalDate.of(0, 8, 20), LEO),
    Arguments.of(LocalDate.of(0, 8, 21), LEO),
    Arguments.of(LocalDate.of(0, 8, 22), LEO),
    Arguments.of(LocalDate.of(0, 8, 23), LEO),
    /* TEST VIRGO */
    Arguments.of(LocalDate.of(0, 8, 24), VIRGO),
    Arguments.of(LocalDate.of(0, 8, 25), VIRGO),
    Arguments.of(LocalDate.of(0, 8, 26), VIRGO),
    Arguments.of(LocalDate.of(0, 8, 27), VIRGO),
    Arguments.of(LocalDate.of(0, 8, 28), VIRGO),
    Arguments.of(LocalDate.of(0, 8, 29), VIRGO),
    Arguments.of(LocalDate.of(0, 8, 30), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 1), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 2), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 3), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 4), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 5), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 6), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 7), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 8), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 9), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 10), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 11), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 12), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 13), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 14), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 15), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 16), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 17), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 18), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 19), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 20), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 21), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 22), VIRGO),
    Arguments.of(LocalDate.of(0, 9, 23), VIRGO),
    /* TEST LIBRA */
    Arguments.of(LocalDate.of(0, 9, 24), LIBRA),
    Arguments.of(LocalDate.of(0, 9, 25), LIBRA),
    Arguments.of(LocalDate.of(0, 9, 26), LIBRA),
    Arguments.of(LocalDate.of(0, 9, 27), LIBRA),
    Arguments.of(LocalDate.of(0, 9, 28), LIBRA),
    Arguments.of(LocalDate.of(0, 9, 29), LIBRA),
    Arguments.of(LocalDate.of(0, 9, 30), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 1), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 2), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 3), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 4), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 5), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 6), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 7), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 8), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 9), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 10), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 11), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 12), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 13), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 14), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 15), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 16), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 17), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 18), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 19), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 20), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 21), LIBRA),
    Arguments.of(LocalDate.of(0, 10, 22), LIBRA),
    /* TEST SCORPIO */
    Arguments.of(LocalDate.of(0, 10, 23), SCORPIO),
    Arguments.of(LocalDate.of(0, 10, 24), SCORPIO),
    Arguments.of(LocalDate.of(0, 10, 25), SCORPIO),
    Arguments.of(LocalDate.of(0, 10, 26), SCORPIO),
    Arguments.of(LocalDate.of(0, 10, 27), SCORPIO),
    Arguments.of(LocalDate.of(0, 10, 28), SCORPIO),
    Arguments.of(LocalDate.of(0, 10, 29), SCORPIO),
    Arguments.of(LocalDate.of(0, 10, 30), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 1), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 2), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 3), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 4), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 5), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 6), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 7), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 8), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 9), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 10), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 11), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 12), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 13), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 14), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 15), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 16), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 17), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 18), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 19), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 20), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 21), SCORPIO),
    Arguments.of(LocalDate.of(0, 11, 22), SCORPIO),
    /* TEST SAGITTARIUS */
    Arguments.of(LocalDate.of(0, 11, 23), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 11, 24), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 11, 25), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 11, 26), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 11, 27), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 11, 28), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 11, 29), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 11, 30), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 1), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 2), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 3), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 4), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 5), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 6), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 7), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 8), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 9), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 10), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 11), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 12), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 13), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 14), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 15), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 16), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 17), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 18), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 19), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 20), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 21), SAGITTARIUS),
    Arguments.of(LocalDate.of(0, 12, 22), SAGITTARIUS),
    /* TEST CAPRICORN */
    Arguments.of(LocalDate.of(0, 12, 23), CAPRICORN),
    Arguments.of(LocalDate.of(0, 12, 24), CAPRICORN),
    Arguments.of(LocalDate.of(0, 12, 25), CAPRICORN),
    Arguments.of(LocalDate.of(0, 12, 26), CAPRICORN),
    Arguments.of(LocalDate.of(0, 12, 27), CAPRICORN),
    Arguments.of(LocalDate.of(0, 12, 28), CAPRICORN),
    Arguments.of(LocalDate.of(0, 12, 29), CAPRICORN),
    Arguments.of(LocalDate.of(0, 12, 30), CAPRICORN),
  )
}