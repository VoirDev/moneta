package dev.voir.moneta

/**
 * Metadata that gives a [Moneta] value its monetary context.
 *
 * [decimals] is the number of fractional digits used by the currency's smallest unit.
 * For example, USD commonly uses `2`, JPY uses `0`, and BTC uses `8`.
 *
 * [code] is an optional ISO-style or token-style identifier such as `"USD"` or `"BTC"`.
 * [symbol] is an optional display marker such as `"$"` or `"₿"`.
 */
data class Currency(
    /** Number of fractional digits used for display scaling and atomic conversions. */
    val decimals: Int = 2,

    /** Optional currency code or asset identifier. */
    val code: String? = null,

    /** Optional display symbol for UI layers that choose to show one. */
    val symbol: String? = null,
)

/**
 * Backward-compatible alias for callers that prefer the longer monetary name.
 */
typealias MonetaCurrency = Currency
