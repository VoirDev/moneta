package dev.voir.moneta

import dev.voir.decimal.Decimal
import dev.voir.decimal.Rounding

/**
 * Convert a `source` Moneta into a new Moneta expressed in [targetCurrency].
 *
 * The [rate] is interpreted as target units per one source unit. For example, converting
 * `1.00 USD` with a rate of `4.6` into MYR produces `4.60 MYR`.
 *
 * @param rate exchange rate as target currency per one unit of this instance's currency
 * @param targetCurrency currency metadata stored on the converted [Moneta]
 * @param rounding rounding mode applied when scaling to [targetCurrency.decimals]
 * @return converted [Moneta] using [targetCurrency]
 * @throws ArithmeticException if [rate] is zero
 */
fun Moneta.convertByRate(
    rate: Decimal,
    targetCurrency: Currency,
    rounding: Rounding = Rounding.HALF_UP
): Moneta {
    if (rate == Decimal.zero()) {
        throw ArithmeticException("Cannot convert: rate is zero")
    }

    // Multiply source amount by rate (target per 1 source) -> amount in target currency.
    val raw = this.value.multiply(rate)

    // Reuse Moneta's Decimal normalization so rounding and absolute-value behavior stay consistent.
    return Moneta.fromDecimal(
        raw,
        currency = targetCurrency,
        rounding = rounding
    )
}

/**
 * Calculate the exchange rate `targetPerOneSource` between two `Moneta` amounts that represent
 * the *same monetary value* expressed in different currencies.
 *
 * Example: if `from = 1.00 USD` and `to = 4.6 MYR`, the returned Decimal is
 * approximately `4.6` (MYR per 1 USD).
 *
 * @param from   amount in source currency (denominator). Must be non-zero.
 * @param to     equivalent amount in target currency (numerator).
 * @param scale  number of fractional digits to compute for the resulting rate (precision).
 *               Choose enough digits to represent crypto rates (e.g. 12).
 * @param rounding rounding used when dividing to compute the rate.
 * @return exchange rate expressed as [to] currency per one [from] currency unit
 * @throws ArithmeticException if `from.value` is zero.
 */
fun calculateExchangeRate(
    from: Moneta,
    to: Moneta,
    scale: Int = 12,
    rounding: Rounding = Rounding.HALF_UP
): Decimal {
    // Defensive: do not call platform division if divisor is zero.
    if (from.value == Decimal.zero()) throw ArithmeticException("Cannot compute exchange rate: source amount is zero")
    return to.value.divide(from.value, scale, rounding)
}

/**
 * Calculate the reverse exchange rate (source per 1 target) from a previously computed rate.
 *
 * Equivalent to `1 / rate`. Throws when [rate] is zero.
 *
 * @param rate    exchange rate decimal (target per 1 source)
 * @param scale   fractional digits for the inverse
 * @param rounding rounding used when dividing to compute the inverse
 * @return reverse exchange rate expressed as source per one target unit
 * @throws ArithmeticException if [rate] is zero
 */
fun calculateReverseExchangeRate(
    rate: Decimal,
    scale: Int = 12,
    rounding: Rounding = Rounding.HALF_UP
): Decimal {
    if (rate == Decimal.zero()) throw ArithmeticException("Cannot compute reverse exchange rate: rate is zero")
    return Decimal.ofInteger("1").divide(rate, scale, rounding)
}

/**
 * Convenience: compute both direct (targetPerOneSource) and reverse (sourcePerOneTarget) rates.
 *
 * Returns Pair<direct, reverse>.
 *
 * @param from amount in source currency; must be non-zero
 * @param to equivalent amount in target currency
 * @param scale fractional digits used for both direct and reverse rates
 * @param rounding rounding used for the two divisions
 * @return pair where `first` is the direct rate and `second` is the reverse rate
 */
fun calculateExchangeRatesPair(
    from: Moneta,
    to: Moneta,
    scale: Int = 12,
    rounding: Rounding = Rounding.HALF_UP
): Pair<Decimal, Decimal> {
    val direct = calculateExchangeRate(from, to, scale, rounding)
    val reverse = calculateReverseExchangeRate(direct, scale, rounding)
    return direct to reverse
}
