package dev.voir.moneta

import dev.voir.decimal.Decimal
import dev.voir.decimal.Rounding

/**
 * Converts this amount into another currency using an exchange rate.
 *
 * The exact product is rounded once to the scale of [targetCurrency].
 *
 * ```
 * Moneta.fromInt(1, usd).convertByRate(Decimal.parse("4.6"), myr) // 4.60 MYR
 * ```
 *
 * @param rate Positive rate expressed as target units per one unit of this currency.
 * @param targetCurrency Currency of the converted amount.
 * @param rounding Rounding applied at the scale of [targetCurrency].
 * @return Converted amount in [targetCurrency].
 * @throws IllegalArgumentException When [rate] is zero or negative.
 */
public fun Moneta.convertByRate(
    rate: Decimal,
    targetCurrency: Currency,
    rounding: Rounding = Rounding.HALF_UP,
): Moneta {
    requirePositiveRate(rate)
    return Moneta.fromDecimal(value * rate, targetCurrency, rounding)
}

/**
 * Calculates the exchange rate between two amounts that represent the same value.
 *
 * For `from = 1.00 USD` and `to = 4.60 MYR` the rate is `4.6` MYR per USD. A very small rate
 * can round to zero when [scale] is too low; choose a scale that fits the currencies involved.
 *
 * @param from Non-zero amount in the source currency.
 * @param to Non-zero equivalent amount in the target currency.
 * @param scale Maximum number of fractional digits of the rate.
 * @param rounding Rounding applied to the rate.
 * @return Rate expressed as [to] units per one [from] unit.
 * @throws IllegalArgumentException When [from] or [to] is zero, or [scale] is negative.
 */
public fun calculateExchangeRate(
    from: Moneta,
    to: Moneta,
    scale: Int = 12,
    rounding: Rounding = Rounding.HALF_UP,
): Decimal {
    requireNonZeroAmount(from, "from")
    requireNonZeroAmount(to, "to")
    return to.value.divide(from.value, scale, rounding)
}

/**
 * Calculates the reverse of an exchange rate, `1 / rate`.
 *
 * When both amounts are available, prefer [calculateExchangeRatesPair], which computes the
 * reverse rate from the amounts instead of from an already rounded rate.
 *
 * @param rate Positive rate expressed as target units per one source unit.
 * @param scale Maximum number of fractional digits of the reverse rate.
 * @param rounding Rounding applied to the reverse rate.
 * @return Rate expressed as source units per one target unit.
 * @throws IllegalArgumentException When [rate] is zero or negative, or [scale] is negative.
 */
public fun calculateReverseExchangeRate(
    rate: Decimal,
    scale: Int = 12,
    rounding: Rounding = Rounding.HALF_UP,
): Decimal {
    requirePositiveRate(rate)
    return Decimal.one().divide(rate, scale, rounding)
}

/**
 * Calculates the direct and reverse exchange rates between two amounts that represent the same
 * value.
 *
 * Both rates are computed from the amounts, so each is rounded once and neither inherits the
 * rounding error of the other.
 *
 * @param from Non-zero amount in the source currency.
 * @param to Non-zero equivalent amount in the target currency.
 * @param scale Maximum number of fractional digits of both rates.
 * @param rounding Rounding applied to both rates.
 * @return Pair of the direct rate ([to] per [from]) and the reverse rate ([from] per [to]).
 * @throws IllegalArgumentException When [from] or [to] is zero, or [scale] is negative.
 */
public fun calculateExchangeRatesPair(
    from: Moneta,
    to: Moneta,
    scale: Int = 12,
    rounding: Rounding = Rounding.HALF_UP,
): Pair<Decimal, Decimal> {
    requireNonZeroAmount(from, "from")
    requireNonZeroAmount(to, "to")
    val direct = to.value.divide(from.value, scale, rounding)
    val reverse = from.value.divide(to.value, scale, rounding)
    return direct to reverse
}

/**
 * Requires an exchange rate to be positive.
 *
 * @param rate Candidate rate.
 * @throws IllegalArgumentException When [rate] is zero or negative.
 */
private fun requirePositiveRate(rate: Decimal) {
    require(rate.signum() > 0) { "Exchange rate must be positive, but was $rate." }
}

/**
 * Requires an amount used as an exchange-rate operand to be non-zero.
 *
 * @param amount Candidate amount.
 * @param name Parameter name used in the error message.
 * @throws IllegalArgumentException When [amount] is zero.
 */
private fun requireNonZeroAmount(amount: Moneta, name: String) {
    require(!amount.isZero()) { "Cannot calculate an exchange rate: $name amount $amount is zero." }
}
