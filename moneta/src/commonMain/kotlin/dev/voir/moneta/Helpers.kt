package dev.voir.moneta

import dev.voir.decimal.Rounding

/**
 * Create [Moneta] from an [Int] interpreted as whole currency units.
 *
 * @param currency currency metadata stored on the resulting [Moneta]
 * @param rounding rounding mode used while scaling to [Currency.decimals]
 * @return [Moneta] representing this integer as whole units
 */
fun Int.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromInt(
    value = this,
    currency = currency,
    rounding = rounding
)

/**
 * Create [Moneta] from a [Long] interpreted as whole currency units.
 *
 * @param currency currency metadata stored on the resulting [Moneta]
 * @param rounding rounding mode used while scaling to [Currency.decimals]
 * @return [Moneta] representing this long as whole units
 */
fun Long.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromLong(
        value = this,
        currency = currency,
        rounding = rounding
    )

/**
 * Create [Moneta] from a [Short] interpreted as whole currency units.
 *
 * @param currency currency metadata stored on the resulting [Moneta]
 * @param rounding rounding mode used while scaling to [Currency.decimals]
 * @return [Moneta] representing this short as whole units
 */
fun Short.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromShort(
        value = this,
        currency = currency,
        rounding = rounding
    )

/**
 * Create [Moneta] from a [Byte] interpreted as whole currency units.
 *
 * @param currency currency metadata stored on the resulting [Moneta]
 * @param rounding rounding mode used while scaling to [Currency.decimals]
 * @return [Moneta] representing this byte as whole units
 */
fun Byte.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromByte(
        value = this,
        currency = currency,
        rounding = rounding
    )

/**
 * Create [Moneta] from a [Double] interpreted as decimal whole currency units.
 *
 * Prefer string input for authoritative money values when possible.
 *
 * @param currency currency metadata stored on the resulting [Moneta]
 * @param rounding rounding mode used while scaling to [Currency.decimals]
 * @return [Moneta] representing this double as whole units
 */
fun Double.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromDouble(
        value = this,
        currency = currency,
        rounding = rounding
    )

/**
 * Create [Moneta] from a [Float] interpreted as decimal whole currency units.
 *
 * Prefer string input for authoritative money values when possible.
 *
 * @param currency currency metadata stored on the resulting [Moneta]
 * @param rounding rounding mode used while scaling to [Currency.decimals]
 * @return [Moneta] representing this float as whole units
 */
fun Float.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromFloat(
        value = this,
        currency = currency,
        rounding = rounding
    )

/**
 * Create [Moneta] from a generic [Number].
 *
 * Known primitive types are routed to their dedicated factories; other implementations
 * are parsed from `toString()`.
 *
 * @param currency currency metadata stored on the resulting [Moneta]
 * @param rounding rounding mode used while scaling to [Currency.decimals]
 * @return [Moneta] representing this number as whole units
 */
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
 * Create [Moneta] from an [Int] interpreted as atomic smallest units.
 *
 * For a currency with `decimals == 2`, `100.toAtomicMoneta(currency)` represents `1.00`.
 *
 * @param currency currency metadata whose [Currency.decimals] controls decimal placement
 * @param rounding rounding mode used after converting atomic units to decimal units
 * @return [Moneta] represented by this atomic integer
 */
fun Int.toAtomicMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromAtomicInt(
    value = this,
    currency = currency,
    rounding = rounding
)

/**
 * Create [Moneta] from a [Long] interpreted as atomic smallest units.
 *
 * @param currency currency metadata whose [Currency.decimals] controls decimal placement
 * @param rounding rounding mode used after converting atomic units to decimal units
 * @return [Moneta] represented by this atomic long
 */
fun Long.toAtomicMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromAtomicLong(
    value = this,
    currency = currency,
    rounding = rounding
)

/**
 * Create [Moneta] from a string containing an atomic smallest-unit integer.
 *
 * @param currency currency metadata whose [Currency.decimals] controls decimal placement
 * @param rounding rounding mode used after converting atomic units to decimal units
 * @return [Moneta] represented by this atomic integer string
 * @throws IllegalArgumentException when the receiver is not a valid integer string
 */
fun String.toAtomicMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromAtomicString(
    value = this,
    currency = currency,
    rounding = rounding
)

/**
 * Operator alias for [Moneta.plus].
 *
 * @param other amount to add
 * @return sum using the left operand's currency metadata
 */
operator fun Moneta.plus(other: Moneta): Moneta = this.plus(other)

/**
 * Operator alias for [Moneta.minus].
 *
 * @param other amount to subtract
 * @return difference using the left operand's currency metadata
 */
operator fun Moneta.minus(other: Moneta): Moneta = this.minus(other)

/**
 * Operator alias for multiplying [Moneta] by an [Int] whole-number factor.
 *
 * @param factor integer multiplier
 * @return product using the left operand's currency metadata
 */
operator fun Moneta.times(factor: Int): Moneta = this.times(factor.toLong())
