package dev.voir.moneta

import dev.voir.decimal.Decimal
import dev.voir.decimal.Rounding

/**
 * Creates an amount of this many whole currency units.
 *
 * @param currency Currency of the amount.
 * @return Amount equivalent to [Moneta.fromInt].
 */
public fun Int.toMoneta(currency: Currency = Currency()): Moneta = Moneta.fromInt(this, currency)

/**
 * Creates an amount of this many whole currency units.
 *
 * @param currency Currency of the amount.
 * @return Amount equivalent to [Moneta.fromLong].
 */
public fun Long.toMoneta(currency: Currency = Currency()): Moneta = Moneta.fromLong(this, currency)

/**
 * Creates an amount of this many whole currency units.
 *
 * @param currency Currency of the amount.
 * @return Amount equivalent to [Moneta.fromShort].
 */
public fun Short.toMoneta(currency: Currency = Currency()): Moneta = Moneta.fromShort(this, currency)

/**
 * Creates an amount of this many whole currency units.
 *
 * @param currency Currency of the amount.
 * @return Amount equivalent to [Moneta.fromByte].
 */
public fun Byte.toMoneta(currency: Currency = Currency()): Moneta = Moneta.fromByte(this, currency)

/**
 * Creates an amount from this [Double] in whole currency units.
 *
 * @param currency Currency of the amount.
 * @param rounding Rounding applied at the currency scale.
 * @return Amount equivalent to [Moneta.fromDouble].
 * @throws IllegalArgumentException When this value is `NaN` or infinite.
 */
public fun Double.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP,
): Moneta = Moneta.fromDouble(this, currency, rounding)

/**
 * Creates an amount from this [Float] in whole currency units.
 *
 * @param currency Currency of the amount.
 * @param rounding Rounding applied at the currency scale.
 * @return Amount equivalent to [Moneta.fromFloat].
 * @throws IllegalArgumentException When this value is `NaN` or infinite.
 */
public fun Float.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP,
): Moneta = Moneta.fromFloat(this, currency, rounding)

/**
 * Creates an amount from this [Number] in whole currency units.
 *
 * @param currency Currency of the amount.
 * @param rounding Rounding applied at the currency scale.
 * @return Amount equivalent to [Moneta.fromNumber].
 * @throws IllegalArgumentException When this value is not finite or not plain decimal text.
 */
public fun Number.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP,
): Moneta = Moneta.fromNumber(this, currency, rounding)

/**
 * Creates an amount from this [Decimal] in whole currency units.
 *
 * @param currency Currency of the amount.
 * @param rounding Rounding applied at the currency scale.
 * @return Amount equivalent to [Moneta.fromDecimal].
 */
public fun Decimal.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP,
): Moneta = Moneta.fromDecimal(this, currency, rounding)

/**
 * Creates an amount from this plain decimal text in whole currency units.
 *
 * @param currency Currency of the amount.
 * @param rounding Rounding applied at the currency scale.
 * @return Amount equivalent to [Moneta.fromDecimalString].
 * @throws IllegalArgumentException When this text is not plain decimal text.
 */
public fun String.toMoneta(
    currency: Currency = Currency(),
    rounding: Rounding = Rounding.HALF_UP,
): Moneta = Moneta.fromDecimalString(this, currency, rounding)

/**
 * Creates an amount from this count of the currency's smallest unit.
 *
 * @param currency Currency whose [Currency.decimals] places the decimal point.
 * @return Amount equivalent to [Moneta.fromAtomicInt].
 */
public fun Int.toAtomicMoneta(currency: Currency = Currency()): Moneta =
    Moneta.fromAtomicInt(this, currency)

/**
 * Creates an amount from this count of the currency's smallest unit.
 *
 * @param currency Currency whose [Currency.decimals] places the decimal point.
 * @return Amount equivalent to [Moneta.fromAtomicLong].
 */
public fun Long.toAtomicMoneta(currency: Currency = Currency()): Moneta =
    Moneta.fromAtomicLong(this, currency)

/**
 * Creates an amount from this integer text counting the currency's smallest unit.
 *
 * @param currency Currency whose [Currency.decimals] places the decimal point.
 * @return Amount equivalent to [Moneta.fromAtomicString].
 * @throws IllegalArgumentException When this text is not integer text.
 */
public fun String.toAtomicMoneta(currency: Currency = Currency()): Moneta =
    Moneta.fromAtomicString(this, currency)
