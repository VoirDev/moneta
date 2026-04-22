package dev.voir.moneta

data class Currency(
    val code: String = "default",
    val decimals: Int = 2,
    val symbol: String? = null,
)
