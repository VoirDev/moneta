package dev.voir.moneta

/**
 * Convert a `source` Moneta into a new Moneta expressed in [targetCurrency].
 *
 * @param rate exchange rate.
 * @param targetCode the currency target code stored on the resulting Moneta
 * @param targetDecimals target number of fractional digits for this currency
 * @param rounding rounding mode applied when scaling to the target currency decimals.
 *
 * @throws ArithmeticException if the provided rate is zero or NaN.
 */
fun Moneta.convertByRate(
    rate: Decimal,
    targetCode: String,
    targetDecimals: Int,
    rounding: Rounding = Rounding.HALF_UP
): Moneta {
    if (isDecimalZeroOrNaN(rate)) {
        throw ArithmeticException("Cannot convert: rate is zero or NaN")
    }

    // Multiply source amount by rate (target per 1 source) → amount in target currency (as Decimal).
    val raw = this.value.multiply(rate)

    // Round the resulting Decimal to the target currency decimals (final monetary representation).
    val rounded = raw.setScale(targetDecimals, rounding)

    // Build and return a Moneta in target currency from the decimal string (uses public factory).
    return Moneta.fromDecimalString(
        rounded.toPlainString(),
        code = targetCode,
        decimals = targetDecimals,
        rounding = rounding
    )
}

/**
 * Calculate the exchange rate `targetPerOneSource` between two `Moneta` amounts that represent
 * the *same monetary value* expressed in different currencies.
 *
 * Example: if `from = 1.00 USD` and `to = 4.6 MYR`, the returned Decimal ≈ `4.6` (MYR per 1 USD).
 *
 * @param from   amount in source currency (denominator). Must be non-zero.
 * @param to     equivalent amount in target currency (numerator).
 * @param scale  number of fractional digits to compute for the resulting rate (precision).
 *               Choose enough digits to represent crypto rates (e.g. 12).
 * @param rounding rounding used when dividing to compute the rate.
 * @throws ArithmeticException if `from.value` is zero or NaN.
 */
fun calculateExchangeRate(
    from: Moneta,
    to: Moneta,
    scale: Int = 12,
    rounding: Rounding = Rounding.HALF_UP
): Decimal {
    // Defensive: do not call platform division if divisor is zero (iOS would raise Obj-C exception).
    if (isDecimalZeroOrNaN(from.value)) throw ArithmeticException("Cannot compute exchange rate: source amount is zero or NaN")
    return to.value.divide(from.value, scale, rounding)
}

/**
 * Calculate the reverse exchange rate (source per 1 target) from a previously computed rate.
 *
 * Equivalent to `1 / rate`. Throws on zero/NaN rate.
 *
 * @param rate    exchange rate decimal (target per 1 source)
 * @param scale   fractional digits for the inverse
 * @param rounding rounding used when dividing to compute the inverse
 */
fun calculateReverseExchangeRate(
    rate: Decimal,
    scale: Int = 12,
    rounding: Rounding = Rounding.HALF_UP
): Decimal {
    // 1 / rate
    if (isDecimalZeroOrNaN(rate)) throw ArithmeticException("Cannot compute reverse exchange rate: rate is zero or NaN")
    return Decimal.ofInteger("1").divide(rate, scale, rounding)
}

/**
 * Convenience: compute both direct (targetPerOneSource) and reverse (sourcePerOneTarget) rates.
 *
 * Returns Pair<direct, reverse>.
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

/**
 * Helper: defensive check for Decimal zero or NaN.
 * We treat NaN as invalid divisor.
 */
private fun isDecimalZeroOrNaN(d: Decimal): Boolean {
    // Try to use integer representation if available; otherwise parse string.
    // Prefer toBaseInteger check: if toIntegerString == "0" and scaled appropriately.
    // Simpler portable check: compare to "0" after removing sign/scale.
    val s = d.toPlainString().trim()
    if (s.isEmpty()) return true
    if (s == "NaN" || s.equals("NaN", ignoreCase = true)) return true
    // Normalize: remove optional leading +/-
    val withoutSign = if (s.startsWith('-') || s.startsWith('+')) s.substring(1) else s
    // Strip leading zeros and optional decimal point and fractional zeros
    val cleaned = withoutSign.trimStart('0')
    // If all zeros or empty or just ".000" -> treat as zero
    if (cleaned.isEmpty()) return true
    // If cleaned starts with '.' then could still be zeros, check fraction part
    if (cleaned.all { it == '.' || it == '0' }) return true
    return false
}
