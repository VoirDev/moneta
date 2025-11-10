package dev.voir.moneta

/**
 * Supported rounding modes used by the `Decimal` abstraction and `Money` operations.
 *
 * - **HALF_UP** – Round to nearest; if exactly halfway, round upward (away from zero).
 * - **DOWN**    – Always drop the fractional remainder (toward zero).
 * - **UP**      – Always increase magnitude (away from zero).
 */
enum class Rounding { HALF_UP, DOWN, UP }

/**
 * Platform-independent, lossless **arbitrary precision decimal number**.
 *
 * This type provides a minimal surface for working with money and crypto
 * without `Double` rounding errors and without forcing consumers to depend
 * on `BigDecimal` (JVM) or `NSDecimalNumber` (iOS).
 *
 * `Decimal` is declared as `expect` here, and each platform supplies an `actual`
 * implementation that binds to the most efficient native high-precision type:
 *
 * | Platform        | Backend                                                 |
 * |------------------|---------------------------------------------------------|
 * | JVM / Android    | `java.math.BigDecimal`                                  |
 * | iOS / macOS      | `Foundation.Decimal` / `NSDecimalNumber`                |
 */
expect class Decimal {
    companion object {

        /**
         * Creates a `Decimal` from a text-based decimal representation.
         *
         * @param decimalString A string representing a decimal number, must match:
         *        `[+-]?[0-9]+(\.[0-9]+)?`
         *
         * Examples: `"0"`, `"123"`, `"3.14159"`, `"-42.0"`.
         *
         * @return A new, immutable `Decimal` instance.
         *
         * @throws IllegalArgumentException If the string has invalid format.
         */
        fun of(decimalString: String): Decimal

        /**
         * Creates a `Decimal` from an integer-only string.
         *
         * Identical to `of("123")` but enforces that no decimal point is present
         * and allows platform implementations to optimize construction.
         *
         * Useful when working with database / API / blockchain representations
         * that always store *atomic integers* (e.g. cents, satoshis, wei).
         *
         * @param integerString A base-10 integer without decimals.
         *
         * Examples: `"0"`, `"5000"`, `"-42"`.
         */
        fun ofInteger(integerString: String): Decimal

        /**
         * Returns a canonical `Decimal` representing zero (`0`).
         */
        fun zero(): Decimal
    }

    /**
     * Adds two decimal numbers and returns a new instance.
     */
    fun add(other: Decimal): Decimal

    /**
     * Subtracts `other` from this and returns a new instance.
     */
    fun subtract(other: Decimal): Decimal

    /**
     * Multiplies two decimals.
     *
     * Precision rules follow the native backend (BigDecimal/NSDecimalNumber/etc.).
     */
    fun multiply(other: Decimal): Decimal

    /**
     * Multiplies this decimal by an integer represented as a string.
     *
     * Useful when multiplying a decimal by a whole number without parsing
     * through `Decimal.ofInteger(...)` first.
     */
    fun multiply(integerStr: String): Decimal

    /**
     * Divides this decimal by another with explicit scale and rounding.
     *
     * @param other     Divisor (must not be zero)
     * @param scale     Number of decimal digits to retain after the division
     * @param rounding  Rounding mode (HALF_UP, DOWN, UP)
     *
     * @return A new rounded decimal result.
     *
     * @throws ArithmeticException If dividing by zero.
     */
    fun divide(other: Decimal, scale: Int, rounding: Rounding): Decimal

    /**
     * Divides this decimal by an integer literal represented as string.
     *
     * Equivalent to: `this.divide(Decimal.ofInteger(integerStr), scale, rounding)`
     */
    fun divide(integerStr: String, scale: Int, rounding: Rounding): Decimal

    /**
     * Moves the decimal point to the left (dividing by 10ⁿ).
     *
     * Example: `"12345".movePointLeft(2)` → `"123.45"`
     *
     * Used for scaling atomic units (e.g., satoshis → BTC, cents → dollars).
     */
    fun movePointLeft(places: Int): Decimal

    /**
     * Moves the decimal point to the right (multiplying by 10ⁿ).
     *
     * Example: `"1.23".movePointRight(2)` → `"123"`
     *
     * Used when storing decimals as atomic units.
     */
    fun movePointRight(places: Int): Decimal

    /**
     * Sets the scale (number of decimal digits) and applies rounding.
     *
     * @param scale    Number of fractional decimal digits to retain.
     * @param rounding Rounding mode when trimming digits.
     *
     * @return A new `Decimal` instance with adjusted scale.
     */
    fun setScale(scale: Int, rounding: Rounding): Decimal

    /**
     * Returns a printable base-10 decimal string without scientific notation.
     *
     * Always preserves scale and sign:
     * - `"123.4500"`
     * - `"-0.001"`
     * - `"0"`
     */
    fun toPlainString(): String

    /**
     * Returns only the integer part with *no decimal point*.
     *
     * Examples:
     * - `"123.45" → "12345"` after scaling (if scale was applied earlier)
     * - `"10" → "10"`
     *
     * Used by `Money` when storing into databases or persistence layers.
     */
    fun toIntegerString(): String

    /**
     * Returns a new `Decimal` with a non-negative sign.
     *
     * ### Examples
     * `Decimal.of("-1.23").abs().toPlainString()` → `"1.23"`
     * `Decimal.of("0").abs()` → `"0"`
     */
    fun abs(): Decimal

    /**
     * Returns `true` if this `Decimal` represents a "not a number" value.
     */
    fun isNan(): Boolean
}
