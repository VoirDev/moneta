package dev.voir.moneta

/**
 * Convenient Kotlin extension on primitives:
 *  1000.toMoney(usd) == Money.fromInt(1000, usd)
 */
fun Int.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Money =
    Money.fromInt(this, currency, rounding)

fun Long.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Money =
    Money.fromLong(this, currency, rounding)

fun Short.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Money =
    Money.fromShort(this, currency, rounding)

fun Byte.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Money =
    Money.fromByte(this, currency, rounding)

fun Double.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Money =
    Money.fromDouble(this, currency, rounding)

fun Float.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Money =
    Money.fromFloat(this, currency, rounding)

fun Number.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Money =
    Money.fromNumber(this, currency, rounding)

/**
 * Helpers to create Money from atomic numeric literals with clearer names:
 *  - 100L.toAtomicMoney(usd) -> treats 100 as "100 cents" (if usd.decimals==2)
 */
fun Int.toAtomicMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Money =
    Money.fromAtomicInt(this, currency, rounding)

fun Long.toAtomicMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Money =
    Money.fromAtomicLong(this, currency, rounding)

fun String.toAtomicMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Money =
    Money.fromAtomicString(this, currency, rounding)

/**
 * Provide operator convenience on Money at common level (delegates to existing Money functions).
 */
operator fun Money.plus(other: Money): Money = this.plus(other)
operator fun Money.minus(other: Money): Money = this.minus(other)

/**
 * Example: multiply Money by an integer factor returning Money.
 */
operator fun Money.times(factor: Int): Money = this.times(factor.toLong())

/** Convert back to atomic integer string for persistence */
fun Money.toAtomicString(currency: Currency, rounding: Rounding = Rounding.HALF_UP): String =
    this.toAtomicString(currency, rounding)
