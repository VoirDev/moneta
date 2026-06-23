package dev.voir.moneta

import dev.voir.decimal.Decimal
import dev.voir.decimal.Rounding

/**
 * Compact, immutable wrapper for a monetary amount that **includes currency metadata**.
 *
 * `Moneta` stores two pieces of information together:
 *  1. the high-precision decimal amount (`value`)
 *  2. the currency metadata (`currency`)
 *
 * The associated [Currency] contains:
 *  - `code` — currency identifier, e.g. "USD", "EUR"
 *  - `decimals` — number of fractional digits used for atomic conversions
 *  - `symbol` — optional display symbol, e.g. "$", "€"
 *
 * Internally the amount is stored as a high-precision [Decimal] instance. `Moneta` is
 * **decimal-based** and intended to be exact for fiat and crypto usage when combined with
 * the correct [Currency.decimals] for the currency.
 *
 * Examples:
 * ```
 * val usd = Currency(code = "USD", decimals = 2, symbol = "$")
 * val btc = Currency(code = "BTC", decimals = 8, symbol = "₿")
 *
 * // create 1.235 USD and round to the USD scale (2 decimals) using default HALF_UP:
 * val m = Moneta.fromDecimalString("1.235", currency = usd) // -> 1.24 USD
 *
 * // construct from a whole-unit Int with USD scale:
 * val m2 = Moneta.fromInt(1, currency = usd) // -> 1.00 USD
 *
 * // atomic conversions: build from atomic smallest units (cents, satoshis, etc.)
 * val satoshiAmount = Moneta.fromAtomicLong(150000000L, currency = btc)
 *
 * // get an atomic integer string for persistence (e.g. store cents or satoshis)
 * val atomicString = m.toAtomicString() // e.g. "124"
 * ```
 *
 * Notes:
 * - Prefer `fromDecimalString(...)` when you have an authoritative decimal text input
 *   (user input, JSON, CSV) to avoid floating-point parsing artifacts.
 * - Floating constructors (`fromDouble`, `fromFloat`) should only be used when the
 *   primitive value is already an acceptable source of truth. Prefer [fromDecimalString]
 *   for user input, JSON payloads, and other authoritative decimal text.
 * - Public factory methods normalize input to an absolute value. Arithmetic methods operate on
 *   stored decimal values directly, so subtraction or negative factors can produce signed results.
 *
 * @property value underlying high-precision decimal value
 * @property currency currency metadata stored with the amount
 */
class Moneta private constructor(
    val value: Decimal,
    val currency: Currency,
) {

    /**
     * Factory and helper constructors for `Moneta`.
     *
     * All factory methods accept a [Currency] so the returned `Moneta` is a
     * self-contained monetary value (value + currency metadata).
     *
     * Conventions:
     * - Integer primitives (`Int`, `Long`, `Short`, `Byte`) are interpreted as *whole units*
     *   of the currency. Example: `fromInt(1, currency = usd)` -> `1.00` USD when `usd.decimals == 2`.
     * - Floating primitives (`Double`, `Float`) are converted through [Decimal]. Prefer
     *   [fromDecimalString] when exact textual decimal input is the source of truth.
     * - Atomic constructors (`fromAtomic*`) expect the smallest unit count (cents, satoshis,
     *   wei) and build the decimal by moving the point left by `currency.decimals`.
     *
     * Rounding:
     * - When scaling to `currency.decimals` the default rounding mode is `Rounding.HALF_UP`.
     *   You may pass a different `rounding` parameter to control behavior where needed.
     */
    companion object Companion {
        /**
         * Internal constructor bridge used by factory and conversion helpers.
         *
         * The incoming [value] is scaled to [currency.decimals] with [rounding] and stored
         * as an absolute amount, matching the public factories' current input-normalization model.
         *
         * @param value decimal amount before currency-scale normalization
         * @param currency metadata to attach to the created [Moneta]
         * @param rounding rounding mode used when [value] has more fractional digits than allowed
         * @return normalized monetary value with [currency] metadata
         */
        internal fun fromDecimal(
            value: Decimal,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP,
        ): Moneta = Moneta(
            value = value.setScale(currency.decimals, rounding).abs(),
            currency = currency,
        )

        /**
         * Construct from an integer whole-unit value.
         *
         * @param value whole units (e.g. `1` => `1.00` for USD if `currency.decimals == 2`)
         * @param currency currency metadata stored on the resulting Moneta
         * @param rounding how to round when scaling to currency decimals (default HALF_UP)
         * @return `Moneta` representing `value` in the given currency
         */
        fun fromInt(
            value: Int,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromDecimal(
            value = Decimal.fromInt(value),
            currency = currency,
            rounding = rounding,
        )

        /**
         * Construct from a Long whole-unit value.
         *
         * Same semantics as [fromInt] but accepts larger ranges.
         *
         * @param value whole units to represent
         * @param currency currency metadata stored on the resulting [Moneta]
         * @param rounding rounding mode used while scaling to [Currency.decimals]
         * @return [Moneta] representing [value] whole currency units
         */
        fun fromLong(
            value: Long,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromDecimal(
            value = Decimal.fromLong(value),
            currency = currency,
            rounding = rounding,
        )

        /**
         * Construct from Short (delegates to [fromInt]).
         *
         * @param value whole units to represent
         * @param currency currency metadata stored on the resulting [Moneta]
         * @param rounding rounding mode used while scaling to [Currency.decimals]
         * @return [Moneta] representing [value] whole currency units
         */
        fun fromShort(
            value: Short,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromInt(
            value = value.toInt(),
            currency = currency,
            rounding = rounding
        )

        /**
         * Construct from Byte (delegates to [fromInt]).
         *
         * @param value whole units to represent
         * @param currency currency metadata stored on the resulting [Moneta]
         * @param rounding rounding mode used while scaling to [Currency.decimals]
         * @return [Moneta] representing [value] whole currency units
         */
        fun fromByte(
            value: Byte,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromInt(
            value.toInt(),
            currency = currency,
            rounding = rounding
        )

        /**
         * Construct from [Double] whole-unit input.
         *
         * [Decimal.fromDouble] is used for conversion. Prefer [fromDecimalString] when
         * you already have canonical decimal text.
         *
         * @param value the Double value interpreted as whole currency units
         * @param currency currency metadata stored on the resulting [Moneta]
         * @param rounding rounding mode used while scaling to [Currency.decimals]
         * @return [Moneta] representing [value] in whole currency units
         */
        fun fromDouble(
            value: Double,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromDecimal(
            value = Decimal.fromDouble(value),
            currency = currency,
            rounding = rounding,
        )

        /**
         * Construct from [Float] whole-unit input.
         *
         * Decimal does not expose a dedicated Float constructor, so the Float is converted
         * from its Kotlin string representation. Prefer [fromDecimalString] when you
         * already have canonical decimal text.
         *
         * @param value the Float value interpreted as whole currency units
         * @param currency currency metadata stored on the resulting [Moneta]
         * @param rounding rounding mode used while scaling to [Currency.decimals]
         * @return [Moneta] representing [value] in whole currency units
         */
        fun fromFloat(
            value: Float,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromDecimal(
            value = Decimal.of(value.toString()),
            currency = currency,
            rounding = rounding,
        )

        /**
         * Construct from a textual decimal where the text is authoritative.
         *
         * Use this when you want deterministic decimal parsing (recommended for
         * user-entered amounts, JSON payloads with decimal strings, etc.).
         *
         * Example: `fromDecimalString("0.1", usd)` -> exactly `0.10` USD.
         *
         * @param value decimal representation (e.g. "123.45", "-0.001"; sign is normalized away)
         * @param currency currency metadata including code and decimal precision
         * @param rounding rounding mode used while scaling to [Currency.decimals]
         * @return [Moneta] parsed from [value] and normalized to [currency]
         */
        fun fromDecimalString(
            value: String,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromDecimal(
            value = Decimal.of(value),
            currency = currency,
            rounding = rounding,
        )

        /**
         * Construct from an atomic integer (smallest unit count) provided as Int.
         *
         * Example: `fromAtomicInt(150, usd)` -> `1.50` USD when `usd.decimals == 2`.
         *
         * @param value count of smallest units (integer)
         * @param currency currency metadata whose [Currency.decimals] controls point movement
         * @param rounding rounding mode used after moving the decimal point left
         * @return [Moneta] represented by [value] atomic units
         */
        fun fromAtomicInt(
            value: Int,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromAtomicString(
            value = value.toString(),
            currency = currency,
            rounding = rounding,
        )

        /**
         * Construct from atomic units provided as Long.
         *
         * Use for large atomic counts such as long-running ledger aggregates.
         *
         * @param value count of smallest units
         * @param currency currency metadata whose [Currency.decimals] controls point movement
         * @param rounding rounding mode used after moving the decimal point left
         * @return [Moneta] represented by [value] atomic units
         */
        fun fromAtomicLong(
            value: Long,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromAtomicString(
            value = value.toString(),
            currency = currency,
            rounding = rounding,
        )

        /**
         * Construct from an atomic integer represented as a decimal string.
         *
         * This is convenient for persisted data (databases, blockchain, APIs) that
         * already store atomic amounts as strings.
         *
         * @param value integer string (e.g. "12345")
         * @param currency currency metadata whose [Currency.decimals] controls point movement
         * @param rounding rounding mode used after moving the decimal point left
         * @return [Moneta] represented by [value] atomic units
         */
        fun fromAtomicString(
            value: String,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromDecimal(
            value = Decimal.ofInteger(value).movePointLeft(currency.decimals),
            currency = currency,
            rounding = rounding,
        )

        /**
         * Generic constructor accepting any Kotlin [Number].
         *
         * Routes to the most appropriate specific constructor for known types.
         * - Int/Long/Short/Byte → whole units
         * - Double/Float        → converted through their dedicated factory behavior
         *
         * For unknown `Number` subclasses the `toString()` representation is parsed.
         *
         * @param value numeric source value
         * @param currency currency metadata stored on the resulting [Moneta]
         * @param rounding rounding mode used while scaling to [Currency.decimals]
         * @return [Moneta] created using the matching primitive factory where possible
         */
        fun fromNumber(
            value: Number,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            return when (value) {
                is Int -> fromInt(value, currency = currency, rounding = rounding)
                is Long -> fromLong(value, currency = currency, rounding = rounding)
                is Short -> fromShort(value, currency = currency, rounding = rounding)
                is Byte -> fromByte(value, currency = currency, rounding = rounding)
                is Double -> fromDouble(value, currency = currency, rounding = rounding)
                is Float -> fromFloat(value, currency = currency, rounding = rounding)
                else -> {
                    fromDecimal(
                        value = Decimal.of(value.toString()),
                        currency = currency,
                        rounding = rounding,
                    )
                }
            }
        }

        /**
         * Returns a zero-valued `Moneta` (decimal zero) with default currency metadata.
         *
         * @return zero amount with default [Currency] metadata
         */
        fun zero() = Moneta(Decimal.zero(), currency = Currency())
    }

    /**
     * Add two monetary amounts. Currencies should match externally; this method only
     * performs decimal addition of the underlying values and keeps this instance's currency.
     *
     * Prefer to check currency equality before adding in your business logic.
     *
     * @param other amount to add to this value
     * @return sum using this instance's [currency]
     */
    fun plus(other: Moneta): Moneta = Moneta(this.value.add(other.value), this.currency)

    /**
     * Subtract another monetary amount from this. The result can be negative.
     *
     * As with [plus], currencies should match prior to subtraction.
     *
     * @param other amount to subtract from this value
     * @return difference using this instance's [currency]
     */
    fun minus(other: Moneta): Moneta = Moneta(this.value.subtract(other.value), this.currency)

    /**
     * Multiply the monetary amount by an integer factor. Negative factors produce negative results.
     *
     * Useful for quantity multiplication, fee scaling, etc.
     *
     * @param factor integer multiplier
     * @return product using this instance's [currency]
     */
    fun times(factor: Long): Moneta =
        Moneta(this.value.multiplyInteger(factor.toString()), this.currency)

    /**
     * Multiply the monetary amount by an arbitrary decimal factor. Negative factors produce negative results.
     *
     * Use when applying fractional multipliers or normalized rates.
     *
     * @param factor decimal multiplier
     * @return product using this instance's [currency]
     */
    fun timesDecimal(factor: Decimal): Moneta = Moneta(this.value.multiply(factor), this.currency)

    /**
     * Divide the monetary amount by an integer divisor. Negative divisors produce negative results.
     *
     * @param factor integer divisor
     * @param scale intermediate division scale (default 18) used to preserve precision
     * @param rounding rounding mode applied to the quotient
     * @return quotient using this instance's [currency]
     */
    fun divide(factor: Long, scale: Int = 18, rounding: Rounding = Rounding.HALF_UP): Moneta =
        Moneta(this.value.divideInteger(factor.toString(), scale, rounding), this.currency)

    /**
     * Return this value with different currency metadata.
     *
     * The underlying decimal amount is preserved and then normalized to [currency.decimals].
     * This is useful when a calculator result is re-applied to a newly selected currency.
     *
     * @param currency currency metadata to attach to the returned [Moneta]
     * @param rounding rounding mode used while normalizing to [currency.decimals]
     * @return new [Moneta] with the same amount and new currency metadata
     */
    fun withCurrency(
        currency: Currency,
        rounding: Rounding = Rounding.HALF_UP,
    ): Moneta = fromDecimal(
        value = value,
        currency = currency,
        rounding = rounding,
    )

    /**
     * Return this monetary value with a different decimal amount.
     *
     * @param value new decimal amount before currency-scale normalization
     * @param rounding rounding mode used while normalizing to this instance's currency precision
     * @return new [Moneta] with [value] and this instance's [currency]
     */
    fun withValue(
        value: Decimal,
        rounding: Rounding = Rounding.HALF_UP,
    ): Moneta = fromDecimal(
        value = value,
        currency = currency,
        rounding = rounding,
    )

    /**
     * Parse decimal text and return this monetary value with the parsed amount.
     *
     * @param value decimal text returned by a calculator or other final-value source
     * @param rounding rounding mode used while normalizing to this instance's currency precision
     * @return new [Moneta] with parsed [value] and this instance's [currency]
     * @throws IllegalArgumentException when [value] is not valid decimal text
     */
    fun withDecimalString(
        value: String,
        rounding: Rounding = Rounding.HALF_UP,
    ): Moneta = withValue(
        value = Decimal.of(value),
        rounding = rounding,
    )

    /**
     * Replace the amount from atomic smallest units using this instance's currency precision.
     *
     * @param value atomic smallest-unit amount
     * @param rounding rounding mode used after converting atomic units to decimal units
     * @return new [Moneta] represented by [value] atomic units and this instance's [currency]
     */
    fun withAtomicLong(
        value: Long,
        rounding: Rounding = Rounding.HALF_UP,
    ): Moneta = fromAtomicLong(
        value = value,
        currency = currency,
        rounding = rounding,
    )

    /**
     * Render the monetary value as plain decimal text.
     *
     * When [scale] is null, this returns [Decimal.toPlainString], which trims insignificant
     * trailing zeros. When [scale] is provided, the value is rounded and padded to exactly
     * that many fractional digits using [Decimal.toFormattedString] without grouping.
     *
     * @param scale exact fraction digit count to render, or null for the Decimal plain form
     * @return decimal text without currency code, symbol, or digit grouping
     */
    fun toDecimalString(scale: Int? = null): String {
        return if (scale == null) {
            value.toPlainString()
        } else {
            value.toFormattedString(scale, scale, Rounding.HALF_UP, '.', null)
        }
    }

    /**
     * Format a [Moneta] amount as grouped decimal text for display.
     *
     * Formatting is delegated to [dev.voir.decimal.Decimal.toFormattedString], with
     * [currency][Moneta.currency] precision used as the maximum fractional precision when
     * [decimals] is null.
     *
     * @param decimals exact fractional digit count to show, or null to show up to [Currency.decimals]
     * @param groupSeparator character inserted between groups of three integer digits
     * @param decimalSeparator character inserted between integer and fractional digits
     * @param showDecimalIfZero when [decimals] is null, controls whether whole values show one
     * fractional zero (for example, `1 234.0`) or no fractional part (`1 234`)
     * @param appendSymbol whether to append [Currency.symbol] when present
     * @return formatted decimal text, optionally suffixed with [Currency.symbol]
     * @throws IllegalArgumentException when [decimals] is outside `0..currency.decimals`
     */
    fun toFormattedString(
        decimals: Int? = null,
        groupSeparator: Char = ' ',
        decimalSeparator: Char = '.',
        showDecimalIfZero: Boolean = true,
        appendSymbol: Boolean = false,
    ): String {
        require(decimals == null || (decimals in 0..(this.currency.decimals))) {
            "decimals must be null or between 0 and currency.decimals"
        }

        val maxFractionDigits = decimals ?: this.currency.decimals
        val minFractionDigits = decimals ?: if (showDecimalIfZero && maxFractionDigits > 0) 1 else 0

        // Decimal owns grouping, rounding, zero trimming, and padding; Moneta supplies money precision.
        val formatted = value.toFormattedString(
            maxFractionDigits,
            minFractionDigits,
            Rounding.HALF_UP,
            decimalSeparator,
            groupSeparator,
        )

        return if (appendSymbol && currency.symbol != null) {
            formatted + currency.symbol
        } else {
            formatted
        }
    }

    /**
     * Convert stored monetary value to an **atomic integer string** for persistence or transport.
     *
     * This moves the decimal point *right* by `currency.decimals` and rounds to an integer
     * (using provided `rounding`) so it's safe to store/serialize as the smallest unit.
     *
     * Example: for USD (`currency.decimals == 2`)
     *  - value = 1.235, toAtomicString -> "124" with HALF_UP
     *
     * @param rounding rounding mode to use when rounding to atomic integer
     * @return base-10 integer string of smallest units
     */
    fun toAtomicString(rounding: Rounding = Rounding.HALF_UP): String {
        val shifted = value
            .setScale(this.currency.decimals, rounding)
            .movePointRight(this.currency.decimals)
        return shifted.toIntegerString()
    }

    /**
     * Convert this Moneta into atomic smallest-units as a Long (cents, sats, wei, ...).
     *
     * - Rounds to `currency.decimals` (HALF_UP by default), then shifts right by that precision
     * - Returns null if the result doesn't fit in a Long
     *
     * @param rounding rounding mode applied before shifting to atomic units
     * @return atomic smallest-unit amount, or null when it does not fit in [Long]
     */
    fun toAtomicLongOrNull(rounding: Rounding = Rounding.HALF_UP): Long? {
        val atomicStr = this.toAtomicString(rounding)
        return atomicStr.toLongOrNull()
    }

    /**
     * Human-friendly textual representation of the monetary value using the underlying decimal.
     *
     * Equivalent to `value.toPlainString()` which avoids scientific notation.
     * This does not append currency code or symbol.
     *
     * @return plain decimal text for [value]
     */
    override fun toString(): String = value.toPlainString()
}
