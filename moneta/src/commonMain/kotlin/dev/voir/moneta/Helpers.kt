package dev.voir.moneta

/**
 * Convenient Kotlin extension on primitives:
 *  1000.toMoneta() == Money.fromInt(1000)
 */
fun Int.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromInt(
    value = this,
    currency = currency,
    rounding = rounding
)

fun Long.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromLong(
        value = this,
        currency = currency,
        rounding = rounding
    )

fun Short.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromShort(
        value = this,
        currency = currency,
        rounding = rounding
    )

fun Byte.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromByte(
        value = this,
        currency = currency,
        rounding = rounding
    )

fun Double.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromDouble(
        value = this,
        currency = currency,
        rounding = rounding
    )

fun Float.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromFloat(
        value = this,
        currency = currency,
        rounding = rounding
    )

fun Number.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromNumber(
        value = this,
        currency = currency,
        rounding = rounding
    )

/**
 * Helpers to create Money from atomic numeric literals with clearer names:
 *  - 100L.toAtomicMoney(usd) -> treats 100 as "100 cents" (if usd.decimals==2)
 */
fun Int.toAtomicMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromAtomicInt(
    value = this,
    currency = currency,
    rounding = rounding
)

fun Long.toAtomicMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromAtomicLong(
    value = this,
    currency = currency,
    rounding = rounding
)

fun String.toAtomicMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromAtomicString(
    value = this,
    currency = currency,
    rounding = rounding
)

/**
 * Tries to normalize any user-entered text into a decimal number string.
 *
 * Rules:
 * - Removes all whitespace.
 * - Removes all characters except digits, '.' , ',' and '-'.
 * - Keeps only the leading minus sign; any other minus signs are discarded.
 * - Treats the last '.' or ',' as the decimal separator.
 * - Treats all earlier separators as grouping separators and removes them.
 * - Removes trailing decimal separators with no fractional part.
 * - Returns null if there are no digits left after cleanup.
 *
 * Examples:
 * - "10 000" -> "10000"
 * - "10,5" -> "10.5"
 * - "1.234,56" -> "1234.56"
 * - "1,234.56" -> "1234.56"
 * - "12." -> "12"
 * - "abc" -> null
 */
fun String.toDecimalStringOrNull(): String? {
    val cleaned = this
        .trim()
        .replace("\\s+".toRegex(), "")
        .replace("[^\\d.,-]".toRegex(), "")

    if (cleaned.isBlank()) return null

    val isNegative = cleaned.startsWith("-")
    val unsigned = cleaned.removePrefix("-").replace("-", "")

    if (unsigned.isBlank()) return null

    val lastSeparatorIndex = maxOf(
        unsigned.lastIndexOf('.'),
        unsigned.lastIndexOf(',')
    )

    val hasAnyDigit = unsigned.any(Char::isDigit)
    val endsWithSeparator = unsigned.endsWith('.') || unsigned.endsWith(',')

    if (!hasAnyDigit && !endsWithSeparator) return null

    val normalized = buildString {
        if (isNegative) append('-')

        if (lastSeparatorIndex == -1) {
            append(unsigned.filter(Char::isDigit))
        } else {
            val integer = unsigned
                .substring(0, lastSeparatorIndex)
                .filter(Char::isDigit)

            append(integer.ifEmpty { "0" })
            append('.')

            append(
                unsigned
                    .substring(lastSeparatorIndex + 1)
                    .filter(Char::isDigit)
            )
        }
    }

    return normalized.takeIf { it.any(Char::isDigit) || it.endsWith('.') }
}

operator fun Moneta.plus(other: Moneta): Moneta = this.plus(other)
operator fun Moneta.minus(other: Moneta): Moneta = this.minus(other)
operator fun Moneta.times(factor: Int): Moneta = this.times(factor.toLong())
