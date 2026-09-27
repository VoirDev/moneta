package dev.voir.moneta

/**
 * Metadata that gives a [Moneta] value its monetary context.
 *
 * Two currencies are the same currency only when all three properties are equal. [Moneta]
 * arithmetic and comparison require the same currency on both operands.
 *
 * @property decimals Number of fractional digits of the currency's smallest unit, for example `2`
 * for USD, `0` for JPY, `8` for BTC, and `18` for ETH. Every [Moneta] is rounded to this scale, and
 * atomic conversions move the decimal point by this many places.
 * @property code Optional ISO-style or token-style identifier such as `"USD"` or `"BTC"`.
 * @property symbol Optional display marker such as `"$"` or `"₿"`, appended by
 * [Moneta.toFormattedString] when requested.
 * @throws IllegalArgumentException When [decimals] is negative.
 */
public data class Currency(
    public val decimals: Int = 2,
    public val code: String? = null,
    public val symbol: String? = null,
) {
    init {
        require(decimals >= 0) { "Currency decimals must not be negative, but was $decimals." }
    }
}

/**
 * Alias for callers that prefer the longer monetary name.
 */
public typealias MonetaCurrency = Currency
