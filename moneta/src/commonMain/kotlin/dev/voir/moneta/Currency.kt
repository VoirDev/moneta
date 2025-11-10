package dev.voir.moneta

data class Currency(val code: String, val decimals: Int) {
    init {
        require(decimals >= 0) { "decimals >= 0 required" }
    }

    companion object {
        val Default = Currency("default", 8)
        val Crypto = Currency("crypto", 18)
    }
}
