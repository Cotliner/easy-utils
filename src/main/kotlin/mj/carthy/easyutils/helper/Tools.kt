package mj.carthy.easyutils.helper

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import mj.carthy.easyutils.document.BaseDocument
import mj.carthy.easyutils.enums.ZodiacSign
import mj.carthy.easyutils.enums.ZodiacSign.*
import mj.carthy.easyutils.exception.EntityNotFoundException
import mj.carthy.easyutils.exception.UnprocessedException
import mj.carthy.easyutils.helper.Errors.Companion.ENTITY_NOT_FOUND
import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import mj.carthy.easyutils.helper.Errors.Companion.ZODIAC_SIGN_NOT_FOUND
import mj.carthy.easyutils.model.ErrorDetails
import mj.carthy.easyutils.model.PaginationResult
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.exception.ExceptionUtils.getMessage
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec.Access
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.lang.String.format
import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId.systemDefault
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Function
import java.util.function.Function.identity
import java.util.stream.Collectors

/* CONSTANTS */
const val DASH: String = "-"

/* PATTERN */
const val DATE_PATTERN: String = "dd/MM/yyyy"

/* ERRORS */
const val CAN_NOT_FOUND_ENTITY_WITH_ID_ERROR: String = "Can not found %s with id : %s"
const val CAN_NOT_FOUND_ZODIAC_SIGN_ERROR: String = "Can not found zodiac sign of date %s"

const val SCALE_AFTER_DOT = 2
const val CODE_GENERATOR_LOW_VALUE = 10000
const val CODE_GENERATOR_HIGH_VALUE = 100000

fun error(ex: Exception, request: ServerHttpRequest, status: HttpStatus, code: ErrorCode): ErrorDetails = ErrorDetails(
  Instant.now(),
  getMessage(ex),
  request.uri.rawPath,
  status,
  status.value(),
  code
)

fun error(message: String, request: ServerHttpRequest, status: HttpStatus, code: ErrorCode): ErrorDetails = ErrorDetails(
  Instant.now(),
  message,
  request.uri.rawPath,
  status,
  status.value(),
  code
)

inline fun <reified T> retrieveOrNew(element: T?): T = element ?: T::class.java.getDeclaredConstructor().newInstance()

inline fun <I, reified M> findOrThrow(
  request: (id: I) -> Optional<M>,
  id: I
): M = request(id).orElseThrow{EntityNotFoundException(
  ENTITY_NOT_FOUND,
  format(CAN_NOT_FOUND_ENTITY_WITH_ID_ERROR, M::class.java.simpleName, id)
)}

suspend inline fun <I, reified M> singleOrError(
  request: (id: I) -> Mono<M>,
  id: I
): M = request(id).awaitSingleOrNull() ?: throw entityNotFoundException<I, M>(id)

suspend inline fun <I, reified M> singleOrError(
  request: suspend (id: I) -> M?,
  id: I
): M = request(id) ?: throw entityNotFoundException<I, M>(id)

inline fun <I, reified M> entityNotFoundException(
  id: I
): EntityNotFoundException = EntityNotFoundException(ENTITY_NOT_FOUND, format(CAN_NOT_FOUND_ENTITY_WITH_ID_ERROR, M::class.java.simpleName, id))

/* CLASS EXTENSION: SUSPEND METHOD*/
suspend fun <T> Flux<T>.toSet(): Set<T> = collect(Collectors.toSet()).awaitSingle()

/* CLASS EXTENSION: INLINE METHOD */
inline fun <reified T> ObjectMapper.convert(json: String): T = readValue(json, T::class.java)
inline fun <reified T> Gson.makeFrom(record: ConsumerRecord<String, Map<String, String>>): T = fromJson(toJsonTree(record.value()), T::class.java)
inline fun <P: Any, reified C: P> Flux<P>.safeCast(): Flow<C> = filter { it is C }.map { it as C }.asFlow()
inline fun <P: Any, reified C: P> Flow<P>.safeCast(): Flow<C> = filter { it is C }.map { it as C }

/* CLASS EXTENSION: METHOD */
fun LocalDate.isBetween(before: LocalDate, after: LocalDate): Boolean = (isAfter(before) && isBefore(after)) || equals(before) || equals(after)
fun Instant.isBetween(before: Instant, after: Instant): Boolean = (isAfter(before) && isBefore(after)) || equals(before) || equals(after)

fun LocalDate.format(pattern: String = DATE_PATTERN): String = format(DateTimeFormatter.ofPattern(pattern))

fun Number.format(scaleValue: Number = SCALE_AFTER_DOT): String = BigDecimal(string).setScale(scaleValue.toInt(), HALF_EVEN).string

fun AuthorizeExchangeSpec.emptyAblePathMatchers(vararg antPatterns: String, access: (Access) -> AuthorizeExchangeSpec = Access::permitAll): AuthorizeExchangeSpec = if (antPatterns.isEmpty()) this else access(pathMatchers(*antPatterns))

fun <T> Collection<T>.paginationResult(page: Number, size: Number): PaginationResult<T> = PaginationResult(this, page, size, size)

fun <T> T.consume(consumer: suspend (it: T) -> Unit): Unit = this.run { GlobalScope.launch { consumer.invoke(this@consume) } }
fun <T> Channel<T>.consumeWith(consumer: (it: T) -> Unit): Channel<T> = this.also { GlobalScope.launch { consumeEach(consumer::invoke) } }

fun <K, V> MutableMap<K, V>.putIfIsAbsent(key: K, value: V): V { this[key] = value; return value }

/* CLASS EXTENSION: VALUES */
val code get(): String = (Random().nextInt(CODE_GENERATOR_HIGH_VALUE - CODE_GENERATOR_LOW_VALUE) + CODE_GENERATOR_LOW_VALUE).string
val eighteen: LocalDate get() = LocalDate.now().minusYears(18)

val LocalDate.moreThanEighteen get(): Boolean = isBefore(eighteen) || isEqual(eighteen)
val Instant.moreThanEighteen get(): Boolean = localDate.moreThanEighteen

val DataBuffer.byteForBuffer get(): ByteArray {
  val bytes: ByteArray = byteArrayOf(readableByteCount().toByte())
  read(bytes)
  DataBufferUtils.release(this)
  return bytes
}
val Flux<FilePart>.bytes get(): Flux<ByteArray> = flatMap(FilePart::bytes)
val Mono<FilePart>.bytes get(): Mono<ByteArray> = flatMap(FilePart::bytes)
val FilePart.bytes get(): Mono<ByteArray> = content().map(DataBuffer::byteForBuffer).reduce(ArrayUtils::addAll)
val <T> Collection<T>.isEmptyWithDash get(): Boolean = isEmpty() || (size == 1 && first() == DASH)

val BigDecimal.isPositive get(): Boolean = this > BigDecimal.ZERO

val <K, V: Comparable<V>> Map<K, V>.maxByValue get(): V? = maxByOrNull { it.value }?.value
val <K: Comparable<K>, V> Map<K, V>.maxByKey get(): V? = maxByOrNull { it.key }?.value

val <T: Any> T.string: String get() = toString()
val <T: Any> T.mono: Mono<T> get() = toMono()

val String.uuid: UUID get() = UUID.fromString(this)

val Duration.isPositive get() = seconds > 0

val Instant.isPositive get() = isAfter(Instant.now())
val Instant.isNegative get() = isBefore(Instant.now())

val <ID, T: BaseDocument<ID>> Flux<T>.collectById get(): Mono<Map<ID?, T>> = collectMap(Function(BaseDocument<ID>::id), identity())
val <ID, T: BaseDocument<ID>> Collection<T>.collectById get(): Map<ID?, T> = associateBy { it.id }

val LocalDate.zodiacSign get(): ZodiacSign = when (monthValue) {
  1 -> when (dayOfMonth) { in 1..20 -> CAPRICORN else -> AQUARIUS }
  2 -> when (dayOfMonth) { in 1..18 -> AQUARIUS else -> PISCES }
  3 -> when (dayOfMonth) { in 1..21 -> PISCES else -> ARIES }
  4 -> when (dayOfMonth) { in 1..20 -> ARIES else -> TAURUS }
  5 -> when (dayOfMonth) { in 1..21 -> TAURUS else -> GEMINI }
  6 -> when (dayOfMonth) { in 1..21 -> GEMINI else -> CANCER }
  7 -> when (dayOfMonth) { in 1..23 -> CANCER else -> LEO }
  8 -> when (dayOfMonth) { in 1..23 -> LEO else -> VIRGO }
  9 -> when (dayOfMonth) { in 1..23 -> VIRGO else -> LIBRA }
  10 -> when (dayOfMonth) { in 1..22 -> LIBRA else -> SCORPIO }
  11 -> when (dayOfMonth) { in 1..22 -> SCORPIO else -> SAGITTARIUS }
  12 -> when (dayOfMonth) { in 1..22 -> SAGITTARIUS else -> CAPRICORN }
  else -> throw UnprocessedException(ZODIAC_SIGN_NOT_FOUND, CAN_NOT_FOUND_ZODIAC_SIGN_ERROR(/**/this))
}

val Instant.localDate get(): LocalDate = atZone(systemDefault()).toLocalDate()
val LocalDate.instant get(): Instant = atStartOfDay(systemDefault()).toInstant()

fun Criteria.param(key: String, value: Any?): Criteria = if (value == null) this else and(key).`is`(value)
fun Criteria.param(key: String, value: Any?, direction: Direction?) = if(value == null || direction == null) this else if (direction == ASC) and(key).gte(value) else and(key).lte(key)
fun Query.nullableWith(pageable: Pageable?): Query = if(pageable == null) this else with(pageable)

/* OPERATOR */
operator fun Number.invoke(): BigDecimal = string.toBigDecimal()
operator fun String.invoke(): BigDecimal = toBigDecimal()
operator fun BigDecimal.invoke(): String = toPlainString()
operator fun String.invoke(vararg args: Any): String = String.format(this, *args)