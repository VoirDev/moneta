package dev.voir.moneta

import dev.voir.moneta.Moneta.Companion.fromDouble
import dev.voir.moneta.Moneta.Companion.fromInt

/**
 * Compact, immutable wrapper for a monetary amount that **includes currency metadata**.
 *
 * `Moneta` stores three pieces of information together:
 *  1. the high-precision decimal amount (`value`)
 *  2. the currency code (`code`) — e.g. "USD", "EUR"
 *  3. the currency scale/decimals (`decimals`) — number of fractional digits (e.g. 2 for USD cents)
 *
 * Internally the amount is stored as a high-precision [`Decimal`] instance. `Moneta` is
 * **decimal-based** and intended to be exact for fiat and crypto usage when combined with
 * the correct `decimals` for the currency.
 *
 * Examples:
 * ```
 * // create 1.235 USD and round to the USD scale (decimals = 2) using default HALF_UP:
 * val m = Moneta.fromDecimalString("1.235", code = "USD", decimals = 2) // -> 1.24 USD
 *
 * // construct from a whole-unit Int with USD scale:
 * val m2 = Moneta.fromInt(1, code = "USD", decimals = 2)     // -> 1.00 USD
 *
 * // atomic conversions: build from atomic smallest units (cents, satoshis, etc.)
 * val satoshiAmount = Moneta.fromAtomicLong(150000000L, code = "BTC", decimals = 8)
 *
 * // get an atomic integer string for persistence (e.g. store cents or satoshis)
 * val atomicString = m.toAtomicString()   // e.g. "124"
 * ```
 *
 * Notes:
 * - Prefer `fromDecimalString(...)` when you have an authoritative decimal text input
 *   (user input, JSON, CSV) to avoid floating-point parsing artifacts.
 * - Floating constructors (`fromDouble`, `fromFloat`) parse the `toString()` representation
 *   of the primitive; use them only when you accept potential binary-floating artifacts.
 *
 * @property value underlying high-precision decimal value
 * @property code  currency ISO-like code stored with the amount (default "default")
 * @property decimals number of fractional digits used for atomic conversions (default 4)
 */
class Moneta private constructor(
    val value: Decimal,
    val code: String = "default",
    val decimals: Int = 4,
) {

    /**
     * Factory and helper constructors for `Moneta`.
     *
     * All factory methods accept `code` and `decimals` so the returned `Moneta` is a
     * self-contained monetary value (value + currency metadata). Typical usage passes
     * the appropriate `code` and `decimals` for the currency you're modeling (e.g.
     * "USD", 2). When omitted, defaults are used (see parameters).
     *
     * Conventions:
     * - Integer primitives (`Int`, `Long`, `Short`, `Byte`) are interpreted as *whole units*
     *   of the currency. Example: `fromInt(1, code = "USD", decimals = 2)` -> `1.00` USD.
     * - Floating primitives (`Double`, `Float`) are parsed via their textual `toString()`.
     *   This can introduce floating artifacts; prefer `fromDecimalString` when an exact
     *   textual decimal is the source of truth.
     * - Atomic constructors (`fromAtomic*`) expect the smallest unit count (cents, satoshis,
     *   wei) and build the decimal by moving the point left by `decimals`.
     *
     * Rounding:
     * - When scaling to `decimals` the default rounding mode is `Rounding.HALF_UP`.
     *   You may pass a different `rounding` parameter to control behaviour where needed.
     */
    companion object Companion {
        /**
         * Construct from an integer whole-unit value.
         *
         * @param value whole units (e.g. `1` => `1.00` for USD if `decimals == 2`)
         * @param code the currency code stored on the resulting Moneta
         * @param decimals number of fractional digits for this currency
         * @param rounding how to round when scaling to currency decimals (default HALF_UP)
         * @return `Moneta` representing `value` in the given currency
         */
        fun fromInt(
            value: Int,
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val integerStr = value.toString()
            val dec = Decimal.ofInteger(integerStr)
            val scaled = dec.setScale(decimals, rounding)

            return Moneta(
                value = scaled.abs(),
                code = code,
                decimals = decimals
            )
        }

        /**
         * Construct from a Long whole-unit value.
         *
         * Same semantics as [fromInt] but accepts larger ranges.
         */
        fun fromLong(
            value: Long,
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val integerStr = value.toString()
            val dec = Decimal.ofInteger(integerStr)
            val scaled = dec.setScale(decimals, rounding)

            return Moneta(
                value = scaled.abs(),
                code = code,
                decimals = decimals
            )
        }

        /**
         * Construct from Short (delegates to [fromInt]).
         */
        fun fromShort(
            value: Short,
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromInt(
            value = value.toInt(),
            code = code,
            decimals = decimals,
            rounding = rounding
        )

        /**
         * Construct from Byte (delegates to [fromInt]).
         */
        fun fromByte(
            value: Byte,
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta = fromInt(
            value.toInt(),
            code = code,
            decimals = decimals,
            rounding = rounding
        )

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
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.of(value.toString())
            val scaled = d.setScale(decimals, rounding)

            return Moneta(
                value = scaled.abs(),
                code = code,
                decimals = decimals
            )
        }

        /**
         * Construct from Float by parsing `Float.toString()` into a Decimal.
         *
         * Same caveat as [fromDouble].
         */
        fun fromFloat(
            value: Float,
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.of(value.toString())
            val scaled = d.setScale(decimals, rounding)
            return Moneta(
                value = scaled.abs(),
                code = code,
                decimals = decimals
            )
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
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.of(decimal)
            val scaled = d.setScale(decimals, rounding)
            return Moneta(
                value = scaled.abs(),
                code = code,
                decimals = decimals
            )
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
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.ofInteger(atomic.toString()).movePointLeft(decimals)
            val scaled = d.setScale(decimals, rounding)
            return Moneta(
                value = scaled.abs(),
                code = code,
                decimals = decimals
            )
        }

        /**
         * Construct from atomic units provided as Long.
         *
         * Use for large atomic counts such as long-running ledger aggregates.
         */
        fun fromAtomicLong(
            atomic: Long,
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.ofInteger(atomic.toString()).movePointLeft(decimals)
            val scaled = d.setScale(decimals, rounding)
            return Moneta(
                value = scaled.abs(),
                code = code,
                decimals = decimals
            )
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
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            val d = Decimal.ofInteger(atomic).movePointLeft(decimals)
            val scaled = d.setScale(decimals, rounding)
            return Moneta(
                value = scaled.abs(),
                code = code,
                decimals = decimals
            )
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
            code: String = "default",
            decimals: Int = 4,
            rounding: Rounding = Rounding.HALF_UP
        ): Moneta {
            return when (number) {
                is Int -> fromInt(number, code = code, decimals = decimals, rounding = rounding)
                is Long -> fromLong(number, code = code, decimals = decimals, rounding = rounding)
                is Short -> fromShort(number, code = code, decimals = decimals, rounding = rounding)
                is Byte -> fromByte(number, code = code, decimals = decimals, rounding = rounding)
                is Double -> fromDouble(
                    number,
                    code = code,
                    decimals = decimals,
                    rounding = rounding
                )

                is Float -> fromFloat(number, code = code, decimals = decimals, rounding = rounding)
                else -> {
                    // fallback: use toString()
                    val d = Decimal.of(number.toString())
                    val scaled = d.setScale(decimals, rounding)
                    Moneta(scaled, code = code, decimals = decimals)
                }
            }
        }

        /**
         * Create a `Moneta` from an atomic integer string **without** rounding to currency decimals.
         *
         * Use this when you want to reconstruct the exact stored atomic value as Decimal,
         * then you can call `.toDecimalString(...)` with the desired number of decimals later.
         */
        fun fromAtomicString(
            atomic: String,
            code: String = "default",
            decimals: Int = 4,
        ): Moneta {
            val d = Decimal.ofInteger(atomic).movePointLeft(decimals).abs()
            return Moneta(
                value = d,
                code = code,
                decimals = decimals
            )
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
     * @param rounding rounding mode to use when rounding to atomic integer
     * @return base-10 integer string of smallest units (no sign normalization)
     */
    fun toAtomicString(rounding: Rounding = Rounding.HALF_UP): String {
        val shifted = value.setScale(this.decimals, rounding).movePointRight(this.decimals)
        return shifted.toIntegerString()
    }

    /**
     * Human-friendly textual representation of the monetary value using the underlying decimal.
     *
     * Equivalent to `value.toPlainString()` which avoids scientific notation.
     */
    override fun toString(): String = value.toPlainString()
}
