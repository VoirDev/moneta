package dev.voir.moneta

/**
 * Convenient Kotlin extension on primitives:
 *  1000.toMoney(usd) == Money.fromInt(1000, usd)
 */
fun Int.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Moneta =
    Moneta.fromInt(this, currency, rounding)

fun Long.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Moneta =
    Moneta.fromLong(this, currency, rounding)

fun Short.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Moneta =
    Moneta.fromShort(this, currency, rounding)

fun Byte.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Moneta =
    Moneta.fromByte(this, currency, rounding)

fun Double.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Moneta =
    Moneta.fromDouble(this, currency, rounding)

fun Float.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Moneta =
    Moneta.fromFloat(this, currency, rounding)

fun Number.toMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Moneta =
    Moneta.fromNumber(this, currency, rounding)

/**
 * Helpers to create Money from atomic numeric literals with clearer names:
 *  - 100L.toAtomicMoney(usd) -> treats 100 as "100 cents" (if usd.decimals==2)
 */
fun Int.toAtomicMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Moneta =
    Moneta.fromAtomicInt(this, currency, rounding)

fun Long.toAtomicMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Moneta =
    Moneta.fromAtomicLong(this, currency, rounding)

fun String.toAtomicMoney(currency: Currency, rounding: Rounding = Rounding.HALF_UP): Moneta =
    Moneta.fromAtomicString(this, currency, rounding)

operator fun Moneta.plus(other: Moneta): Moneta = this.plus(other)
operator fun Moneta.minus(other: Moneta): Moneta = this.minus(other)
operator fun Moneta.times(factor: Int): Moneta = this.times(factor.toLong())

/** Convert back to atomic integer string for persistence */
fun Moneta.toAtomicString(currency: Currency, rounding: Rounding = Rounding.HALF_UP): String =
    this.toAtomicString(currency, rounding)
