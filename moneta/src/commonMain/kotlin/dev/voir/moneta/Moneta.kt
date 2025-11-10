package dev.voir.moneta

import dev.voir.moneta.Moneta.Companion.fromDouble
import dev.voir.moneta.Moneta.Companion.fromInt
import kotlin.jvm.JvmInline

/**
 * Compact, immutable wrapper for a monetary amount.
 *
 * Internally stores the amount as a high-precision [`Decimal`] instance.
 * `Moneta` values are **decimal-based** and intended to be exact for fiat and
 * crypto usage when combined with appropriate `Currency.decimals`.

 * Example:
 * ```
 * val usd = Currency("USD", 2)                // cents
 * val m = Moneta.fromDecimalString("1.235", usd) // constructs 1.24 USD (HALF_UP default)
 * val atomic = m.toAtomicString(usd)         // "124"
 * ```
 *
 * @property value underlying high-precision decimal value
 */
@JvmInline
value class Moneta private constructor(val value: Decimal) {

    /**
     * Factory and helper constructors for `Moneta`.
     *
     * The companion provides convenience functions that:
     * - interpret integer primitives (`Int`, `Long`, etc.) as *whole units* of the currency;
     *   e.g. `fromInt(1, usd)` -> `1.00` USD if `usd.decimals == 2`.
     * - accept floating types (`Double`, `Float`) by parsing their `toString()` representation;
     *   use `fromDecimalString` when the textual decimal is the source of truth to avoid
     *   binary-floating artifacts.
     * - accept **atomic** constructors (`fromAtomic*`) which expect smallest-unit counts
     *   (cents, satoshis, wei) and build the decimal by moving the point left by `currency.decimals`.
     */
    companion object Companion {
        /**
         * Construct from an integer whole-unit value.
         *
         * @param value whole units (e.g. `1` => `1.00` for USD if `decimals == 2`)
         * @param currency currency metadata (must provide `decimals`)
         * @param rounding how to round when scaling to currency decimals (default HALF_UP)
         * @return `Moneta` representing `value` in the given currency
         */
        fun fromInt(
            value: Int,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val integerStr = value.toString()
            val dec = Decimal.ofInteger(integerStr)
            val scaled = dec.setScale(currency.decimals, rounding)
            return Moneta(scaled)
        }

        /**
         * Construct from a Long whole-unit value.
         *
         * Same semantics as [fromInt] but accepts larger ranges.
         */
        fun fromLong(
            value: Long,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val integerStr = value.toString()
            val dec = Decimal.ofInteger(integerStr)
            val scaled = dec.setScale(currency.decimals, rounding)
            return Moneta(scaled)
        }

        /**
         * Construct from Short (delegates to [fromInt]).
         */
        fun fromShort(
            value: Short,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromInt(value.toInt(), currency, rounding)

        /**
         * Construct from Byte (delegates to [fromInt]).
         */
        fun fromByte(
            value: Byte,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromInt(value.toInt(), currency, rounding)

        /**
         * Construct from Double by parsing `Double.toString()` into a Decimal.
         *
         * **Important:** binary floating-point may include artifacts (e.g. `0.1`),
         * so prefer `fromDecimalString` when you have a canonical decimal string.
         *
         * @param value the Double value interpreted as decimal text via `toString()`
         */
        fun fromDouble(
            value: Double,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.of(value.toString())
            val scaled = d.setScale(currency.decimals, rounding)
            return Moneta(scaled)
        }

        /**
         * Construct from Float by parsing `Float.toString()` into a Decimal.
         *
         * Same caveat as [fromDouble].
         */
        fun fromFloat(
            value: Float,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.of(value.toString())
            val scaled = d.setScale(currency.decimals, rounding)
            return Moneta(scaled)
        }

        /**
         * Construct from a textual decimal where the text is authoritative.
         *
         * Use this when you want deterministic decimal parsing (recommended for
         * user-entered amounts, JSON payloads with decimal strings, etc.).
         *
         * Example: `fromDecimalString("0.1", usd)` -> exactly `0.10` USD.
         *
         * @param decimal exact decimal representation (e.g. "123.45", "-0.001")
         */
        fun fromDecimalString(
            decimal: String,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.of(decimal)
            val scaled = d.setScale(currency.decimals, rounding)
            return Moneta(scaled)
        }

        /**
         * Construct from an atomic integer (smallest unit count) provided as Int.
         *
         * Example: `fromAtomicInt(150, usd)` -> `1.50` USD when `usd.decimals == 2`.
         *
         * @param atomic count of smallest units (integer)
         */
        fun fromAtomicInt(
            atomic: Int,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.ofInteger(atomic.toString()).movePointLeft(currency.decimals)
            val scaled = d.setScale(currency.decimals, rounding)
            return Moneta(scaled)
        }

        /**
         * Construct from atomic units provided as Long.
         *
         * Use for large atomic counts such as long-running ledger aggregates.
         */
        fun fromAtomicLong(
            atomic: Long,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.ofInteger(atomic.toString()).movePointLeft(currency.decimals)
            val scaled = d.setScale(currency.decimals, rounding)
            return Moneta(scaled)
        }

        /**
         * Construct from an atomic integer represented as a decimal string.
         *
         * This is convenient for persisted data (databases, blockchain, APIs) that
         * already store atomic amounts as strings.
         *
         * @param atomic integer string (e.g. "12345")
         */
        fun fromAtomicString(
            atomic: String,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.ofInteger(atomic).movePointLeft(currency.decimals)
            val scaled = d.setScale(currency.decimals, rounding)
            return Moneta(scaled)
        }

        /**
         * Generic constructor accepting any Kotlin [Number].
         *
         * Routes to the most appropriate specific constructor for known types.
         * - Int/Long/Short/Byte → whole units
         * - Double/Float         → parsed from `toString()` (use exact string constructor to avoid float artifacts)
         *
         * For unknown `Number` subclasses the `toString()` representation is parsed.
         */
        fun fromNumber(
            number: Number,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            return when (number) {
                is Int -> fromInt(number, currency, rounding)
                is Long -> fromLong(number, currency, rounding)
                is Short -> fromShort(number, currency, rounding)
                is Byte -> fromByte(number, currency, rounding)
                is Double -> fromDouble(number, currency, rounding)
                is Float -> fromFloat(number, currency, rounding)
                else -> {
                    // fallback: use toString()
                    val d = Decimal.of(number.toString())
                    val scaled = d.setScale(currency.decimals, rounding)
                    Moneta(scaled)
                }
            }
        }

        /**
         * Create a `Moneta` from an atomic integer string **without** rounding to currency decimals.
         *
         * Use this when you want to reconstruct the exact stored atomic value as Decimal,
         * then you can call `.toDecimalString(...)` with the desired number of decimals later.
         */
        fun fromAtomicString(atomic: String, currency: Currency): Moneta {
            val d = Decimal.ofInteger(atomic).movePointLeft(currency.decimals)
            return Moneta(d)
        }

        /**
         * Returns a zero-valued `Moneta` (decimal zero).
         */
        fun zero() = Moneta(Decimal.zero())
    }

    /**
     * Add two monetary amounts. Currencies should match externally; this method only
     * performs decimal addition of the underlying values.
     *
     * Prefer to check currency equality before adding in your business logic.
     */
    fun plus(other: Moneta): Moneta = Moneta(this.value.add(other.value))

    /**
     * Subtract another monetary amount from this.
     *
     * As with [plus], currencies should match prior to subtraction.
     */
    fun minus(other: Moneta): Moneta = Moneta(this.value.subtract(other.value))

    /**
     * Multiply the monetary amount by an integer factor.
     *
     * Useful for quantity multiplication, fee scaling, etc.
     *
     * @param factor integer multiplier
     */
    fun times(factor: Long): Moneta = Moneta(this.value.multiply(factor.toString()))

    /**
     * Multiply the monetary amount by an arbitrary decimal factor.
     *
     * Use when applying fractional multipliers or normalized rates.
     */
    fun timesDecimal(factor: Decimal): Moneta = Moneta(this.value.multiply(factor))

    /**
     * Divide the monetary amount by an integer divisor.
     *
     * @param factor integer divisor
     * @param scale intermediate division scale (default 18) used to preserve precision
     * @param rounding rounding mode applied to the quotient
     */
    fun divide(factor: Long, scale: Int = 18, rounding: Rounding = Rounding.HALF_UP): Moneta =
        Moneta(this.value.divide(factor.toString(), scale, rounding))

    /**
     * Render the monetary value as a decimal string.
     * Ensures trailing zeros are preserved when `scale` is explicitly requested.
     */
    fun toDecimalString(scale: Int? = null): String {
        return if (scale == null) {
            value.toPlainString()
        } else {
            val scaled = value.setScale(scale, Rounding.HALF_UP).toPlainString()

            // If the backend stripped trailing zeros, pad them back
            val parts = scaled.split('.')
            when {
                scale == 0 -> parts[0]                                // just integer
                parts.size == 1 -> parts[0] + "." + "0".repeat(scale) // no decimal point present
                else -> parts[0] + "." + parts[1].padEnd(scale, '0')  // pad zeros
            }
        }
    }

    /**
     * Convert stored monetary value to an **atomic integer string** for persistence or transport.
     *
     * This moves the decimal point *right* by `currency.decimals` and rounds to an integer
     * (using provided `rounding`) so it's safe to store/serialize as the smallest unit.
     *
     * Example: for USD (decimals=2)
     *  - value = 1.235, toAtomicString -> "124" with HALF_UP
     *
     * @param currency currency metadata (used to determine atomic scale)
     * @param rounding rounding mode to use when rounding to atomic integer
     * @return base-10 integer string of smallest units (no sign normalization)
     */
    fun toAtomicString(currency: Currency, rounding: Rounding = Rounding.HALF_UP): String {
        val shifted = value.setScale(currency.decimals, rounding).movePointRight(currency.decimals)
        return shifted.toIntegerString()
    }

    /**
     * Human-friendly textual representation of the monetary value using the underlying decimal.
     *
     * Equivalent to `value.toPlainString()` which avoids scientific notation.
     */
    override fun toString(): String = value.toPlainString()
}
