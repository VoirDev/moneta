package dev.voir.moneta

/**
 * Convenient Kotlin extension on primitives:
 *  1000.toMoneta(usd) == Money.fromInt(1000, usd)
 */
fun Int.toMoneta(
    code: String = "default",
    decimals: Int = 4,
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromInt(
    value = this,
    code = code,
    decimals = decimals,
    rounding = rounding
)

fun Long.toMoneta(
    code: String = "default",
    decimals: Int = 4,
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromLong(
        value = this,
        code = code,
        decimals = decimals,
        rounding = rounding
    )

fun Short.toMoneta(
    code: String = "default",
    decimals: Int = 4,
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromShort(
        value = this,
        code = code,
        decimals = decimals,
        rounding = rounding
    )

fun Byte.toMoneta(
    code: String = "default",
    decimals: Int = 4,
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromByte(
        value = this,
        code = code,
        decimals = decimals,
        rounding = rounding
    )

fun Double.toMoneta(
    code: String = "default",
    decimals: Int = 4,
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromDouble(
        value = this,
        code = code,
        decimals = decimals,
        rounding = rounding
    )

fun Float.toMoneta(
    code: String = "default",
    decimals: Int = 4,
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromFloat(
        value = this,
        code = code,
        decimals = decimals,
        rounding = rounding
    )

fun Number.toMoneta(
    code: String = "default",
    decimals: Int = 4,
    rounding: Rounding = Rounding.HALF_UP
): Moneta =
    Moneta.fromNumber(
        value = this,
        code = code,
        decimals = decimals,
        rounding = rounding
    )

/**
 * Helpers to create Money from atomic numeric literals with clearer names:
 *  - 100L.toAtomicMoney(usd) -> treats 100 as "100 cents" (if usd.decimals==2)
 */
fun Int.toAtomicMoneta(
    code: String = "default",
    decimals: Int = 4,
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromAtomicInt(
    value = this,
    code = code,
    decimals = decimals,
    rounding = rounding
)

fun Long.toAtomicMoneta(
    code: String = "default",
    decimals: Int = 4,
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromAtomicLong(
    value = this,
    code = code,
    decimals = decimals,
    rounding = rounding
)

fun String.toAtomicMoneta(
    code: String = "default",
    decimals: Int = 4,
    rounding: Rounding = Rounding.HALF_UP
): Moneta = Moneta.fromAtomicString(
    value = this,
    code = code,
    decimals = decimals,
    rounding = rounding
)

operator fun Moneta.plus(other: Moneta): Moneta = this.plus(other)
operator fun Moneta.minus(other: Moneta): Moneta = this.minus(other)
operator fun Moneta.times(factor: Int): Moneta = this.times(factor.toLong())
