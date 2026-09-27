package dev.voir.moneta

import dev.voir.decimal.Decimal
import dev.voir.decimal.Rounding

/**
 * Immutable, non-negative monetary amount together with its [Currency].
 *
 * Every instance holds two invariants:
 *  1. [value] is never negative. Moneta models an amount of money, not a signed balance; the
 *     direction of a transfer belongs to the caller's domain model. Factories and `with*` helpers
 *     store the absolute value of their input, and operations that would produce a negative amount
 *     (subtracting a larger amount, negative factors, divisors, or rates) are rejected.
 *  2. [value] has at most [Currency.decimals] fractional digits. Factories and every arithmetic
 *     operation round to that scale, so an amount always maps exactly to a whole number of atomic
 *     units (cents, satoshis, wei).
 *
 * Arithmetic and comparison require both operands to have the same [Currency]. Equality is numeric
 * and includes the currency: `1.5 USD` equals `1.50 USD` but not `1.5 EUR`.
 *
 * ```
 * val usd = Currency(decimals = 2, code = "USD", symbol = "$")
 *
 * val price = Moneta.fromDecimalString("1.235", usd)   // 1.24 USD, HALF_UP by default
 * val total = price * 3                                 // 3.72 USD
 * val shares = total.split(2)                           // [1.86 USD, 1.86 USD]
 * val cents = total.toAtomicLong()                      // 372
 * ```
 *
 * Prefer [fromDecimalString] or [fromAtomicString] for authoritative input such as user text,
 * JSON, or persisted values; floating-point factories are for values that are already `Double` or
 * `Float` by nature.
 *
 * @property value Non-negative amount with at most [Currency.decimals] fractional digits.
 * @property currency Currency metadata of this amount.
 */
public class Moneta private constructor(
    public val value: Decimal,
    public val currency: Currency,
) : Comparable<Moneta> {

    /**
     * Factories for [Moneta].
     *
     * Every factory stores the absolute value of its input and rounds it to [Currency.decimals].
     * Integer factories interpret their input as whole currency units; `fromAtomic*` factories
     * interpret it as a count of the smallest unit.
     */
    public companion object {
        /**
         * Creates an amount from a decimal value.
         *
         * @param value Amount in whole currency units; its sign is discarded.
         * @param currency Currency of the amount.
         * @param rounding Rounding applied when [value] has more than [Currency.decimals] fractional
         * digits.
         * @return Amount rounded to the currency scale.
         */
        public fun fromDecimal(
            value: Decimal,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP,
        ): Moneta = Moneta(
            value = value.abs().setScale(currency.decimals, rounding),
            currency = currency,
        )

        /**
         * Creates an amount from plain decimal text.
         *
         * This is the preferred factory for user input, JSON, and CSV amounts because the text is
         * parsed exactly, without floating-point artifacts.
         *
         * @param value Plain decimal text such as `"123.45"`; a sign is accepted and discarded.
         * @param currency Currency of the amount.
         * @param rounding Rounding applied when [value] has more than [Currency.decimals] fractional
         * digits.
         * @return Amount rounded to the currency scale.
         * @throws IllegalArgumentException When [value] is not plain decimal text.
         */
        public fun fromDecimalString(
            value: String,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP,
        ): Moneta = fromDecimal(Decimal.parse(value), currency, rounding)

        /**
         * Creates an amount from whole currency units.
         *
         * @param value Whole units, so `1` is `1.00` for a two-decimal currency; its sign is
         * discarded.
         * @param currency Currency of the amount.
         * @return Amount of [value] whole units.
         */
        public fun fromInt(value: Int, currency: Currency = Currency()): Moneta =
            fromDecimal(Decimal.fromInt(value), currency)

        /**
         * Creates an amount from whole currency units.
         *
         * @param value Whole units; its sign is discarded.
         * @param currency Currency of the amount.
         * @return Amount of [value] whole units.
         */
        public fun fromLong(value: Long, currency: Currency = Currency()): Moneta =
            fromDecimal(Decimal.fromLong(value), currency)

        /**
         * Creates an amount from whole currency units.
         *
         * @param value Whole units; its sign is discarded.
         * @param currency Currency of the amount.
         * @return Amount of [value] whole units.
         */
        public fun fromShort(value: Short, currency: Currency = Currency()): Moneta =
            fromInt(value.toInt(), currency)

        /**
         * Creates an amount from whole currency units.
         *
         * @param value Whole units; its sign is discarded.
         * @param currency Currency of the amount.
         * @return Amount of [value] whole units.
         */
        public fun fromByte(value: Byte, currency: Currency = Currency()): Moneta =
            fromInt(value.toInt(), currency)

        /**
         * Creates an amount from a [Double] in whole currency units.
         *
         * The digits printed by `Double.toString()` are used, so `0.1` becomes exactly `0.1`.
         *
         * @param value Finite amount in whole units; its sign is discarded.
         * @param currency Currency of the amount.
         * @param rounding Rounding applied when [value] has more than [Currency.decimals] fractional
         * digits.
         * @return Amount rounded to the currency scale.
         * @throws IllegalArgumentException When [value] is `NaN` or infinite.
         */
        public fun fromDouble(
            value: Double,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP,
        ): Moneta = fromDecimal(Decimal.fromDouble(value), currency, rounding)

        /**
         * Creates an amount from a [Float] in whole currency units.
         *
         * The digits printed by `Float.toString()` are used, so `0.1f` becomes exactly `0.1` rather
         * than the wider binary value a plain `Float.toDouble()` would expose.
         *
         * @param value Finite amount in whole units; its sign is discarded.
         * @param currency Currency of the amount.
         * @param rounding Rounding applied when [value] has more than [Currency.decimals] fractional
         * digits.
         * @return Amount rounded to the currency scale.
         * @throws IllegalArgumentException When [value] is `NaN` or infinite.
         */
        public fun fromFloat(
            value: Float,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP,
        ): Moneta = fromDecimal(
            // Re-parsing the Float's shortest text as a Double keeps its printed digits, and
            // Decimal.fromDouble expands scientific notation such as "1.0E10".
            value = Decimal.fromDouble(value.toString().toDouble()),
            currency = currency,
            rounding = rounding,
        )

        /**
         * Creates an amount from any [Number] in whole currency units.
         *
         * Kotlin primitive numbers use their dedicated factory; other implementations are parsed
         * from `toString()`, which must be plain decimal text.
         *
         * @param value Amount in whole units; its sign is discarded.
         * @param currency Currency of the amount.
         * @param rounding Rounding applied when [value] has more than [Currency.decimals] fractional
         * digits.
         * @return Amount rounded to the currency scale.
         * @throws IllegalArgumentException When [value] is not finite or its text is not plain
         * decimal text.
         */
        public fun fromNumber(
            value: Number,
            currency: Currency = Currency(),
            rounding: Rounding = Rounding.HALF_UP,
        ): Moneta = when (value) {
            is Int -> fromInt(value, currency)
            is Long -> fromLong(value, currency)
            is Short -> fromShort(value, currency)
            is Byte -> fromByte(value, currency)
            is Double -> fromDouble(value, currency, rounding)
            is Float -> fromFloat(value, currency, rounding)
            else -> fromDecimalString(value.toString(), currency, rounding)
        }

        /**
         * Creates an amount from a count of the currency's smallest unit.
         *
         * @param value Atomic units, so `150` is `1.50` for a two-decimal currency; its sign is
         * discarded.
         * @param currency Currency whose [Currency.decimals] places the decimal point.
         * @return Amount of [value] atomic units.
         */
        public fun fromAtomicInt(value: Int, currency: Currency = Currency()): Moneta =
            fromAtomicDecimal(Decimal.fromInt(value), currency)

        /**
         * Creates an amount from a count of the currency's smallest unit.
         *
         * @param value Atomic units; its sign is discarded.
         * @param currency Currency whose [Currency.decimals] places the decimal point.
         * @return Amount of [value] atomic units.
         */
        public fun fromAtomicLong(value: Long, currency: Currency = Currency()): Moneta =
            fromAtomicDecimal(Decimal.fromLong(value), currency)

        /**
         * Creates an amount from integer text counting the currency's smallest unit.
         *
         * Use this for persisted or transported atomic amounts that may exceed [Long], such as wei.
         *
         * @param value Base-10 integer text such as `"12345"`; a sign is accepted and discarded.
         * @param currency Currency whose [Currency.decimals] places the decimal point.
         * @return Amount of [value] atomic units.
         * @throws IllegalArgumentException When [value] is not integer text.
         */
        public fun fromAtomicString(value: String, currency: Currency = Currency()): Moneta =
            fromAtomicDecimal(Decimal.ofInteger(value), currency)

        /**
         * Returns a zero amount.
         *
         * @param currency Currency of the amount.
         * @return Zero in [currency].
         */
        public fun zero(currency: Currency = Currency()): Moneta = Moneta(Decimal.zero(), currency)

        /**
         * Creates an amount from an integer count of atomic units.
         *
         * Moving the point left by [Currency.decimals] places yields at most that many fractional
         * digits, so no rounding is needed.
         *
         * @param atomic Integer count of atomic units.
         * @param currency Currency whose [Currency.decimals] places the decimal point.
         * @return Amount of [atomic] units.
         */
        private fun fromAtomicDecimal(atomic: Decimal, currency: Currency): Moneta =
            Moneta(atomic.abs().movePointLeft(currency.decimals), currency)
    }

    /**
     * Returns whether this amount is zero.
     *
     * @return `true` when [value] is numerically zero.
     */
    public fun isZero(): Boolean = value.isZero()

    /**
     * Adds another amount of the same currency.
     *
     * @param other Amount to add.
     * @return Exact sum in this currency.
     * @throws IllegalArgumentException When [other] has a different currency.
     */
    public operator fun plus(other: Moneta): Moneta {
        requireSameCurrency(other, "add")
        return Moneta(value + other.value, currency)
    }

    /**
     * Subtracts another amount of the same currency.
     *
     * @param other Amount to subtract; must not exceed this amount.
     * @return Exact difference in this currency.
     * @throws IllegalArgumentException When [other] has a different currency or is larger than
     * this amount.
     */
    public operator fun minus(other: Moneta): Moneta {
        requireSameCurrency(other, "subtract")
        require(value >= other.value) {
            "Cannot subtract $other from $this: the result would be negative."
        }
        return Moneta(value - other.value, currency)
    }

    /**
     * Multiplies this amount by a whole-number quantity.
     *
     * @param factor Non-negative quantity.
     * @return Exact product in this currency.
     * @throws IllegalArgumentException When [factor] is negative.
     */
    public operator fun times(factor: Int): Moneta = times(factor.toLong())

    /**
     * Multiplies this amount by a whole-number quantity.
     *
     * @param factor Non-negative quantity.
     * @return Exact product in this currency.
     * @throws IllegalArgumentException When [factor] is negative.
     */
    public operator fun times(factor: Long): Moneta {
        require(factor >= 0) { "Cannot multiply $this by $factor: the factor must not be negative." }
        return Moneta(value * Decimal.fromLong(factor), currency)
    }

    /**
     * Multiplies this amount by a decimal factor using [Rounding.HALF_UP].
     *
     * @param factor Non-negative factor such as a tax rate or share.
     * @return Product rounded to the currency scale.
     * @throws IllegalArgumentException When [factor] is negative.
     */
    public operator fun times(factor: Decimal): Moneta = multiply(factor)

    /**
     * Multiplies this amount by a decimal factor.
     *
     * @param factor Non-negative factor such as a tax rate or share.
     * @param rounding Rounding applied to the product at the currency scale.
     * @return Product rounded to the currency scale.
     * @throws IllegalArgumentException When [factor] is negative.
     */
    public fun multiply(factor: Decimal, rounding: Rounding = Rounding.HALF_UP): Moneta {
        require(factor.signum() >= 0) {
            "Cannot multiply $this by $factor: the factor must not be negative."
        }
        return fromDecimal(value * factor, currency, rounding)
    }

    /**
     * Divides this amount by a whole number using [Rounding.HALF_UP].
     *
     * Rounded quotients do not add back up to this amount; use [split] to share an amount.
     *
     * @param divisor Positive divisor.
     * @return Quotient rounded to the currency scale.
     * @throws IllegalArgumentException When [divisor] is negative.
     * @throws ArithmeticException When [divisor] is zero.
     */
    public operator fun div(divisor: Int): Moneta = divide(Decimal.fromInt(divisor))

    /**
     * Divides this amount by a whole number using [Rounding.HALF_UP].
     *
     * Rounded quotients do not add back up to this amount; use [split] to share an amount.
     *
     * @param divisor Positive divisor.
     * @return Quotient rounded to the currency scale.
     * @throws IllegalArgumentException When [divisor] is negative.
     * @throws ArithmeticException When [divisor] is zero.
     */
    public operator fun div(divisor: Long): Moneta = divide(Decimal.fromLong(divisor))

    /**
     * Divides this amount by a decimal using [Rounding.HALF_UP].
     *
     * @param divisor Positive divisor.
     * @return Quotient rounded to the currency scale.
     * @throws IllegalArgumentException When [divisor] is negative.
     * @throws ArithmeticException When [divisor] is zero.
     */
    public operator fun div(divisor: Decimal): Moneta = divide(divisor)

    /**
     * Divides this amount by a decimal.
     *
     * The exact quotient is rounded once, directly to the currency scale. Rounded quotients do not
     * add back up to this amount; use [split] to share an amount without losing atomic units.
     *
     * @param divisor Positive divisor.
     * @param rounding Rounding applied to the quotient at the currency scale.
     * @return Quotient rounded to the currency scale.
     * @throws IllegalArgumentException When [divisor] is negative.
     * @throws ArithmeticException When [divisor] is zero.
     */
    public fun divide(divisor: Decimal, rounding: Rounding = Rounding.HALF_UP): Moneta {
        require(divisor.signum() >= 0) {
            "Cannot divide $this by $divisor: the divisor must not be negative."
        }
        return Moneta(value.divide(divisor, currency.decimals, rounding), currency)
    }

    /**
     * Splits this amount into [parts] shares that add up exactly to this amount.
     *
     * Shares differ by at most one atomic unit; the leftover units go to the first shares. For
     * example, `10.00 USD` split into 3 is `[3.34, 3.33, 3.33]`.
     *
     * @param parts Number of shares; must be positive.
     * @return [parts] amounts in this currency, largest first.
     * @throws IllegalArgumentException When [parts] is not positive.
     */
    public fun split(parts: Int): List<Moneta> {
        require(parts > 0) { "Cannot split $this into $parts parts: parts must be positive." }

        val decimals = currency.decimals
        val atomic = value.movePointRight(decimals)
        val partCount = Decimal.fromInt(parts)
        val baseAtomic = atomic.divide(partCount, 0, Rounding.DOWN)
        // The remainder is below parts, so it always fits in an Int.
        val leftoverUnits = (atomic - baseAtomic * partCount).toIntegerString().toInt()

        val base = baseAtomic.movePointLeft(decimals)
        val baseWithUnit = base + Decimal.one().movePointLeft(decimals)
        return List(parts) { index ->
            Moneta(if (index < leftoverUnits) baseWithUnit else base, currency)
        }
    }

    /**
     * Returns the same amount in another currency without exchange-rate conversion.
     *
     * Use this to re-label a value, for example when a user picks a different currency for an
     * amount they typed. To convert between currencies, use [convertByRate].
     *
     * @param currency Currency of the returned amount.
     * @param rounding Rounding applied when [currency] has fewer decimals than this currency.
     * @return This amount rounded to the scale of [currency].
     */
    public fun withCurrency(
        currency: Currency,
        rounding: Rounding = Rounding.HALF_UP,
    ): Moneta = fromDecimal(value, currency, rounding)

    /**
     * Returns an amount of this currency with a different value.
     *
     * @param value New amount in whole currency units; its sign is discarded.
     * @param rounding Rounding applied at the currency scale.
     * @return New amount in this currency.
     */
    public fun withValue(
        value: Decimal,
        rounding: Rounding = Rounding.HALF_UP,
    ): Moneta = fromDecimal(value, currency, rounding)

    /**
     * Returns an amount of this currency parsed from plain decimal text.
     *
     * @param value Plain decimal text such as `"2.40"`; a sign is accepted and discarded.
     * @param rounding Rounding applied at the currency scale.
     * @return New amount in this currency.
     * @throws IllegalArgumentException When [value] is not plain decimal text.
     */
    public fun withDecimalString(
        value: String,
        rounding: Rounding = Rounding.HALF_UP,
    ): Moneta = fromDecimalString(value, currency, rounding)

    /**
     * Returns an amount of this currency from a count of its smallest unit.
     *
     * @param value Atomic units; its sign is discarded.
     * @return New amount in this currency.
     */
    public fun withAtomicLong(value: Long): Moneta = fromAtomicLong(value, currency)

    /**
     * Renders the amount as plain decimal text without grouping, code, or symbol.
     *
     * @param scale Exact number of fractional digits to render, or `null` to render the shortest
     * form without trailing zeros.
     * @param rounding Rounding applied when [scale] is below [Currency.decimals].
     * @return Plain decimal text such as `"1.5"` or, with `scale = 2`, `"1.50"`.
     * @throws IllegalArgumentException When [scale] is negative.
     */
    public fun toDecimalString(scale: Int? = null, rounding: Rounding = Rounding.HALF_UP): String {
        if (scale == null) return value.toPlainString()

        require(scale >= 0) { "scale must not be negative, but was $scale." }
        return value.toFormattedString(scale, scale, rounding, '.', null)
    }

    /**
     * Formats the amount for display with digit grouping.
     *
     * @param decimals Exact number of fractional digits to show, or `null` to show up to
     * [Currency.decimals] digits without trailing zeros.
     * @param groupSeparator Separator inserted between groups of three integer digits.
     * @param decimalSeparator Separator between integer and fractional digits; must differ from
     * [groupSeparator].
     * @param showDecimalIfZero When [decimals] is `null`, whether whole amounts keep one fractional
     * zero (`1 234.0`) instead of none (`1 234`).
     * @param appendSymbol Whether to append [Currency.symbol] when it is present.
     * @param rounding Rounding applied when [decimals] is below [Currency.decimals].
     * @return Formatted text such as `"1 234.5"`, optionally followed by the currency symbol.
     * @throws IllegalArgumentException When [decimals] is outside `0..currency.decimals` or both
     * separators are equal.
     */
    public fun toFormattedString(
        decimals: Int? = null,
        groupSeparator: Char = ' ',
        decimalSeparator: Char = '.',
        showDecimalIfZero: Boolean = true,
        appendSymbol: Boolean = false,
        rounding: Rounding = Rounding.HALF_UP,
    ): String {
        require(decimals == null || decimals in 0..currency.decimals) {
            "decimals must be null or in 0..${currency.decimals} for $currency, but was $decimals."
        }

        val maxFractionDigits = decimals ?: currency.decimals
        val minFractionDigits = decimals ?: if (showDecimalIfZero && maxFractionDigits > 0) 1 else 0
        val formatted = value.toFormattedString(
            maxFractionDigits,
            minFractionDigits,
            rounding,
            decimalSeparator,
            groupSeparator,
        )

        val symbol = currency.symbol
        return if (appendSymbol && symbol != null) formatted + symbol else formatted
    }

    /**
     * Returns the amount as integer text counting the currency's smallest unit.
     *
     * The conversion is exact because the amount never has more than [Currency.decimals]
     * fractional digits. Use this to persist or transport amounts that may exceed [Long].
     *
     * @return Base-10 integer text, for example `"124"` for `1.24 USD`.
     */
    public fun toAtomicString(): String = value.movePointRight(currency.decimals).toIntegerString()

    /**
     * Returns the amount as a count of the currency's smallest unit.
     *
     * @return Atomic units, for example `124` for `1.24 USD`.
     * @throws ArithmeticException When the count does not fit in [Long].
     */
    public fun toAtomicLong(): Long = toAtomicLongOrNull()
        ?: throw ArithmeticException("Atomic amount of $this does not fit in Long: ${toAtomicString()}.")

    /**
     * Returns the amount as a count of the currency's smallest unit, or `null` when it is too large.
     *
     * @return Atomic units, or `null` when the count does not fit in [Long].
     */
    public fun toAtomicLongOrNull(): Long? = toAtomicString().toLongOrNull()

    /**
     * Compares amounts of the same currency numerically.
     *
     * @param other Amount to compare with.
     * @return A negative number, zero, or a positive number when this amount is less than, equal
     * to, or greater than [other].
     * @throws IllegalArgumentException When [other] has a different currency.
     */
    override fun compareTo(other: Moneta): Int {
        requireSameCurrency(other, "compare")
        return value.compareTo(other.value)
    }

    /**
     * Returns whether [other] is an amount with the same numeric value and the same currency.
     *
     * @param other Candidate value.
     * @return `true` for equal amounts in equal currencies.
     */
    override fun equals(other: Any?): Boolean =
        other is Moneta && value == other.value && currency == other.currency

    /**
     * Returns a hash code consistent with [equals].
     *
     * @return Hash of the numeric value and currency.
     */
    override fun hashCode(): Int = 31 * value.hashCode() + currency.hashCode()

    /**
     * Returns a debug representation such as `Moneta(1.50 USD)`.
     *
     * The amount is padded to [Currency.decimals]. Use [toDecimalString] or [toFormattedString]
     * for user-facing text.
     *
     * @return Amount and currency code, when present.
     */
    override fun toString(): String {
        val amount = toDecimalString(currency.decimals)
        return currency.code?.let { "Moneta($amount $it)" } ?: "Moneta($amount)"
    }

    /**
     * Requires [other] to have this amount's currency.
     *
     * @param other Second operand.
     * @param operation Verb used in the error message.
     * @throws IllegalArgumentException When the currencies differ.
     */
    private fun requireSameCurrency(other: Moneta, operation: String) {
        require(currency == other.currency) {
            "Cannot $operation amounts in different currencies: $currency and ${other.currency}."
        }
    }
}
