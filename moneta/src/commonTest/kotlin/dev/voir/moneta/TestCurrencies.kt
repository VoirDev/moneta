package dev.voir.moneta

/** US dollar with two decimals. */
internal val USD = Currency(decimals = 2, code = "USD", symbol = "$")

/** Euro with two decimals. */
internal val EUR = Currency(decimals = 2, code = "EUR", symbol = "€")

/** Japanese yen without fractional digits. */
internal val JPY = Currency(decimals = 0, code = "JPY", symbol = "¥")

/** Bitcoin with eight decimals. */
internal val BTC = Currency(decimals = 8, code = "BTC", symbol = "₿")

/** Ether with eighteen decimals. */
internal val ETH = Currency(decimals = 18, code = "ETH")

/** Parses plain decimal text into a [Moneta] of [currency]. */
internal fun money(value: String, currency: Currency = USD): Moneta =
    Moneta.fromDecimalString(value, currency)
