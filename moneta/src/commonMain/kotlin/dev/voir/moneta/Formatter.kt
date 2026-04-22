package dev.voir.moneta

/**
 * Format Money to a grouped string for users.
 *
 * @param decimals         If null -> show up to Moneta.decimals trimming trailing zeros (significant digits).
 *                         If Int -> show exactly that many fractional digits (0..Moneta.decimals).
 * @param groupSeparator   Character between groups of 3 digits in the whole part (default: space).
 * @param decimalSeparator Character between whole and fraction (default: '.').
 * @param showDecimalIfZero When fraction is zero (after trimming) and decimals == null:
 *                           - true: show decimalSeparator followed by a single '0'
 *                           - false: omit the fractional part entirely
 *
 * @return formatted string, e.g. "1 234.56" or "-0.50" etc.
 */
fun Moneta.toGroupedString(
    decimals: Int? = null,
    groupSeparator: Char = ' ',
    decimalSeparator: Char = '.',
    showDecimalIfZero: Boolean = true,
): String {
    require(decimals == null || (decimals in 0..(this.currency.decimals))) {
        "decimals must be null or between 0 and currency.decimals"
    }

    // Determine how many fraction digits to render and whether to trim
    val rendered: String = if (decimals != null) {
        // exact number of decimals: force scale and get plain string
        this.toDecimalString(decimals)
    } else {
        // significant digits up to currency.decimals: get scaled string with currency.decimals,
        // then trim trailing zeros from fraction (but keep at least nothing).
        val raw = this.toDecimalString(this.currency.decimals)
        // raw is like "-123.450000" or "10.000000" or "5"
        raw
    }

    // Now split sign / whole / frac
    val s = rendered
    val sign = if (s.startsWith('-')) "-" else ""
    val unsigned = if (s.startsWith('-') || s.startsWith('+')) s.substring(1) else s

    val parts = unsigned.split('.')
    val wholeRaw = parts[0].ifEmpty { "0" }
    val fracRaw = if (parts.size > 1) parts[1] else ""

    // If decimals == null we should trim trailing zeros from frac; else exact length is already enforced.
    val fracProcessed = if (decimals == null) {
        val trimmed = fracRaw.trimEnd('0')
        trimmed // may be empty
    } else {
        // ensure fracRaw length == decimals by padding/truncation (toDecimalString(decimals) already ensured this)
        fracRaw.padEnd(decimals, '0').take(decimals)
    }

    // Group whole part (from right, groups of 3)
    val groupedWhole = wholeRaw.reversed()
        .chunked(3)
        .joinToString(groupSeparator.toString())
        .reversed()

    // Decide final fraction text
    val fractionToShow = when {
        fracProcessed.isEmpty() -> {
            if (decimals == null) {
                // we trimmed everything
                if (showDecimalIfZero) "0" else "" // show a single zero if requested
            } else {
                // decimals requested but fraction empty -> show zeros of length decimals
                "0".repeat(decimals)
            }
        }

        else -> {
            // if decimals==null we show trimmed significant digits; else exact digits
            if (decimals == null) fracProcessed else fracProcessed
        }
    }

    val shouldIncludeDecimal = if (decimals == null) {
        // If we trimmed and fractionToShow == "0" and showDecimalIfZero==false -> don't include
        if (fracProcessed.isEmpty() && !showDecimalIfZero) false else true
    } else {
        // decimals specified -> always include decimal if decimals > 0
        decimals > 0
    }

    val result = buildString {
        append(sign)
        append(groupedWhole)
        if (shouldIncludeDecimal) {
            append(decimalSeparator)
            append(fractionToShow)
        }
    }

    return result
}
