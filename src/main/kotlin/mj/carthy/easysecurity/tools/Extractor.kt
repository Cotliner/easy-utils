package mj.carthy.easysecurity.tools

import com.google.common.annotations.VisibleForTesting
import org.apache.commons.lang3.math.NumberUtils
import org.springframework.web.server.ServerWebExchange
import java.util.*
import java.util.function.Function
import java.util.regex.Matcher
import java.util.regex.Pattern

const val BEARER = "Bearer"

private const val PATTERN = "^$BEARER *([^ ]+)*$"

private val CHALLENGE_PATTERN = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE)

private const val NOT_MATCH = "The token is incorrect : %s"

private val GET_FIRST_GROUP = Function { matcher: Matcher -> matcher.group(NumberUtils.INTEGER_ONE) }

@VisibleForTesting fun ServerWebExchange.extract(header: String): String? = parse(this.request.headers.getFirst(header))

@VisibleForTesting fun parse(
        input: String?
): String? = when (input) {
    null -> null
    else -> Optional.of(CHALLENGE_PATTERN.matcher(input)).filter { obj: Matcher -> obj.matches() }.map<String>(
            GET_FIRST_GROUP
    ).orElseThrow<IllegalArgumentException> { IllegalArgumentException(String.format(NOT_MATCH, input)) }
}