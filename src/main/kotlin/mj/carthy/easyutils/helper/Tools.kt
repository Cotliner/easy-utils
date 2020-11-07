package mj.carthy.easyutils.helper

import kotlinx.coroutines.reactive.awaitSingle
import mj.carthy.easyutils.document.BaseDocument
import mj.carthy.easyutils.enums.Sexe
import mj.carthy.easyutils.enums.Sexe.FEMALE
import mj.carthy.easyutils.enums.Sexe.MALE
import mj.carthy.easyutils.enums.ZodiacSign
import mj.carthy.easyutils.enums.ZodiacSign.*
import mj.carthy.easyutils.exception.EntityNotFoundException
import mj.carthy.easyutils.exception.PropertyNotFoundException
import mj.carthy.easyutils.exception.UnprocessableEntityException
import mj.carthy.easyutils.helper.Errors.Companion.ENTITY_NOT_FOUND
import mj.carthy.easyutils.helper.Errors.Companion.ErrorCode
import mj.carthy.easyutils.helper.Errors.Companion.ZODIC_SIGN_NOT_FOUND
import mj.carthy.easyutils.model.ErrorDetails
import mj.carthy.easyutils.model.PaginationResult
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.exception.ExceptionUtils.getMessage
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.server.reactive.ServerHttpRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.String.format
import java.math.BigDecimal
import java.math.RoundingMode.HALF_EVEN
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId.systemDefault
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Function.identity
import java.util.stream.Stream
import kotlin.reflect.KFunction1
import kotlin.streams.toList

/*PATTERN*/
const val DATE_PATTERN = "dd/MM/yyyy"
const val NUMBER_PATTERN = "0.00"

/*ERRORS*/
const val PROPERTY_NOT_FOUND = "The property %s is null in class %s with id %s"
const val PROPERTY_NOT_FOUND_WITHOUT_ID = "The property %s is null in class %s"
const val CAN_NOT_FOUND_ENTITY_WITH_ID = "Can not found %s with id : %s"

const val SCALE_AFTER_DOT = 2

/*Formatter*/
val dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
val numberFormatter = DecimalFormat(NUMBER_PATTERN)

val RANDOM = Random()
const val CODE_GENERATOR_LOW_VALUE = 10000
const val CODE_GENERATOR_HIGH_VALUE = 100000
const val CAN_NOT_FOUND_ZODIAC_SIGN = "Can not found zodiac sign of date %s"

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

fun <C : BaseDocument<UUID>, T> throwIfIsNull(
    element: C,
    getter: Function<C, T>,
    propertyName: String
) {
    if (getter.apply(element) == null) {
        val message: String = format(PROPERTY_NOT_FOUND, propertyName, element.javaClass.simpleName, element.id)
        throw PropertyNotFoundException(message, Errors.PROPERTY_NOT_FOUND)
    }
}

fun <T> setIfIsNonNull(setter: Consumer<T>, valueToSet: T) {
    if (valueToSet != null) { setter.accept(valueToSet) }
}

fun <C, T> setIfIsNonNull(
    mainSetter: Consumer<C>,
    element: C,
    cClass: Class<C>,
    setter: BiConsumer<C, T>,
    valueToSet: T
) {
    if (valueToSet != null) {
        val c: C = createOrGet(element, cClass)
        setter.accept(c, valueToSet)
        mainSetter.accept(c)
    }
}

fun <T> createOrGet(element: T, tClass: Class<T>): T = element ?: tClass.getDeclaredConstructor().newInstance()

fun <C, T> throwIfIsNull(
    element: C,
    getter: Function<C, T>,
    propertyName: String
) {
    if (getter.apply(element) == null) {
        val message: String = format(PROPERTY_NOT_FOUND_WITHOUT_ID, propertyName, element);
        throw PropertyNotFoundException(message, Errors.PROPERTY_NOT_FOUND);
    }
}

fun <I, M> findOrThrow(
    request: (id: I) -> Optional<M>,
    mClass: Class<M>,
    id: I
): M = request.invoke(id).orElseThrow{EntityNotFoundException(
    format(CAN_NOT_FOUND_ENTITY_WITH_ID, mClass.simpleName, id),
    ENTITY_NOT_FOUND
)}

suspend fun <I, M> monoOrError(
    request: (id: I) -> Mono<M>,
    mClass: Class<M>,
    id: I
): M = request.invoke(id).switchIfEmpty(monoError(mClass, id)).awaitSingle()

private fun <I, M> monoError(mClass: Class<M>, id: I): Mono<M> = Mono.error(EntityNotFoundException(format(
    CAN_NOT_FOUND_ENTITY_WITH_ID,
    mClass.simpleName,
    id), ENTITY_NOT_FOUND
))

fun <T> T.doAfterTerminate(consumer: KFunction1<T, Unit>): Mono<out T> = Mono.just(this).doAfterTerminate{ consumer.invoke(this) }

suspend fun <T> Mono<out T>.doSeparately(consumer: (elem: T) -> Unit): T = this.flatMap { elem -> Mono.just(
    elem
).doAfterTerminate{consumer.invoke(elem)}}.awaitSingle();

fun LocalDate.isBetween(before: LocalDate, after: LocalDate): Boolean = this.isAfter(before) && this.isBefore(after);

fun LocalDate.isBetween(before: Instant, after: Instant): Boolean = this.isAfter(before.atZone(
    systemDefault()).toLocalDate()
) && this.isBefore(after.atZone(systemDefault()).toLocalDate())

fun Instant.isBetween(before: Instant, after: Instant): Boolean = this.isAfter(before) && this.isBefore(after);

fun Instant.isBetween(before: LocalDate, after: LocalDate): Boolean = this.isAfter(
    before.atStartOfDay(systemDefault()).toInstant()
) && this.isBefore(after.atStartOfDay(systemDefault()).toInstant())

fun Number.format(scaleValue: Number = SCALE_AFTER_DOT): String = BigDecimal(this.toString()).setScale(scaleValue.toInt(), HALF_EVEN).toString()

fun codeGenerator(): String = (RANDOM.nextInt(CODE_GENERATOR_HIGH_VALUE - CODE_GENERATOR_LOW_VALUE) + CODE_GENERATOR_LOW_VALUE).toString()

fun <T> Collection<T>.paginationResult(page: Number, size: Number): PaginationResult<T> = PaginationResult(this, page, size, this.size)

fun <T> Stream<T>.toSet(): Set<T> = this.toList().toSet()
fun <T> Stream<T>.toMutableSet(): MutableSet<T> = this.toList().toMutableSet()

fun <ID, T: BaseDocument<ID>> Flux<T>.collectById(): Mono<MutableMap<ID?, T>> = this.collectMap(Function(BaseDocument<ID>::id), identity())

fun zodiacSign(dateOfBirth: LocalDate): ZodiacSign = when (dateOfBirth.monthValue) {
    1 -> when (dateOfBirth.dayOfMonth) { in 1..20 -> CAPRICORN else -> AQUARIUS }
    2 -> when (dateOfBirth.dayOfMonth) { in 1..18 -> AQUARIUS else -> PISCES }
    3 -> when (dateOfBirth.dayOfMonth) { in 1..21 -> PISCES else -> ARIES }
    4 -> when (dateOfBirth.dayOfMonth) { in 1..20 -> ARIES else -> TAURUS }
    5 -> when (dateOfBirth.dayOfMonth) { in 1..21 -> TAURUS else -> GEMINI }
    6 -> when (dateOfBirth.dayOfMonth) { in 1..21 -> GEMINI else -> CANCER }
    7 -> when (dateOfBirth.dayOfMonth) { in 1..23 -> CANCER else -> LEO }
    8 -> when (dateOfBirth.dayOfMonth) { in 1..23 -> LEO else -> VIRGO }
    9 -> when (dateOfBirth.dayOfMonth) { in 1..23 -> VIRGO else -> LIBRA }
    10 -> when (dateOfBirth.dayOfMonth) { in 1..22 -> LIBRA else -> SCORPIO }
    11 -> when (dateOfBirth.dayOfMonth) { in 1..22 -> SCORPIO else -> SAGITTARIUS }
    12 -> when (dateOfBirth.dayOfMonth) { in 1..22 -> SAGITTARIUS else -> CAPRICORN }
    else -> throw UnprocessableEntityException(format(CAN_NOT_FOUND_ZODIAC_SIGN, dateOfBirth), ZODIC_SIGN_NOT_FOUND);
}

fun DataBuffer.byteForBuffer(): ByteArray {
    val bytes: ByteArray = byteArrayOf(this.readableByteCount().toByte())
    this.read(bytes)
    DataBufferUtils.release(this)
    return bytes
}

fun Flux<FilePart>.getByte(): Flux<ByteArray> = this.flatMap(FilePart::getByte)

fun Mono<FilePart>.getByte(): Mono<ByteArray> = this.flatMap(FilePart::getByte)

fun FilePart.getByte(): Mono<ByteArray> = this.content().map(DataBuffer::byteForBuffer).reduce(ArrayUtils::addAll)

fun Sexe.inversed(): Sexe = when(this) {
    MALE -> FEMALE
    FEMALE -> MALE
}
