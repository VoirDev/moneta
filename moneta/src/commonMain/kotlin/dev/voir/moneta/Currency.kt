package dev.voir.moneta

data class Currency(
    val decimals: Int = 2,
    val code: String? = null,
    val symbol: String? = null,
)

typealias MonetaCurrency = Currency
