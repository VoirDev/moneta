package dev.voir.moneta

data class Currency(val code: String, val decimals: Int) {
    init {
        require(decimals >= 0) { "decimals >= 0 required" }
    }
}
