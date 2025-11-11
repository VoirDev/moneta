package dev.voir.moneta

import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSDecimalNumberHandler
import platform.Foundation.NSRoundingMode

/**
 * iOS actual implementation of the Decimal expect class.
 *
 * Uses NSDecimalNumber for high-precision decimal arithmetic and
 * NSDecimalNumberHandler to control rounding/scale behavior.
 *
 * Note: NSDecimalNumber operations return new immutable numbers; we use
 * decimalNumberByRoundingAccordingToBehavior(...) for rounding to a scale.
 */
actual class Decimal private constructor(private val n: NSDecimalNumber) {

    actual companion object {
        private val DECIMAL_RE = Regex("""^[+-]?\d+(\.\d+)?$""")
        private val INTEGER_RE = Regex("""^[+-]?\d+$""")

        actual fun of(decimalString: String): Decimal {
            val s = decimalString.trim()

            if (s.isEmpty()) throw IllegalArgumentException("Empty decimal string")
            if (!DECIMAL_RE.matches(s)) throw IllegalArgumentException("Invalid decimal string: $decimalString")

            val nd = NSDecimalNumber(s)
            if (nd == NSDecimalNumber.notANumber()) throw IllegalArgumentException("Invalid decimal string: $decimalString")

            return Decimal(nd)
        }

        actual fun ofInteger(integerString: String): Decimal {
            val s = integerString.trim()

            if (s.isEmpty()) throw IllegalArgumentException("Empty integer string")
            if (!INTEGER_RE.matches(s)) throw IllegalArgumentException("Invalid integer string: $integerString")

            val nd = NSDecimalNumber(s)
            if (nd == NSDecimalNumber.notANumber()) throw IllegalArgumentException("Invalid integer string: $integerString")

            return Decimal(nd)
        }

        actual fun zero(): Decimal = Decimal(NSDecimalNumber.zero)
    }

    actual fun add(other: Decimal): Decimal = Decimal(n.decimalNumberByAdding(other.n))

    actual fun subtract(other: Decimal): Decimal = Decimal(n.decimalNumberBySubtracting(other.n))

    actual fun multiply(other: Decimal): Decimal = Decimal(n.decimalNumberByMultiplyingBy(other.n))

    actual fun multiply(integerStr: String): Decimal {
        val s = integerStr.trim()

        if (s.isEmpty()) throw IllegalArgumentException("Empty integer multiplier")
        if (!INTEGER_RE.matches(s)) throw IllegalArgumentException("Invalid integer multiplier: $integerStr")

        val other = NSDecimalNumber(s)
        if (other == NSDecimalNumber.notANumber()) throw IllegalArgumentException("Invalid integer multiplier: $integerStr")

        return Decimal(n.decimalNumberByMultiplyingBy(other))
    }

    /**
     * Divide and immediately round the result to `scale` digits using `rounding`.
     *
     * We perform plain division then apply a rounding handler via
     * decimalNumberByRoundingAccordingToBehavior(...) to ensure we control the resulting scale.
     */

    actual fun divide(other: Decimal, scale: Int, rounding: Rounding): Decimal {
        if (isZeroOrNaN(other.n)) throw ArithmeticException("Division by zero")

        val raw = n.decimalNumberByDividingBy(other.n)
        val handler = createHandler(scale, rounding)
        val rounded = raw.decimalNumberByRoundingAccordingToBehavior(handler)
        return Decimal(rounded)
    }

    actual fun divide(integerStr: String, scale: Int, rounding: Rounding): Decimal {
        val s = integerStr.trim()

        if (s.isEmpty() || !INTEGER_RE.matches(s)) throw IllegalArgumentException("Invalid integer divisor: $integerStr")

        val other = NSDecimalNumber(s)
        if (isZeroOrNaN(other)) throw ArithmeticException("Division by zero")

        val raw = n.decimalNumberByDividingBy(other)
        val handler = createHandler(scale, rounding)
        val rounded = raw.decimalNumberByRoundingAccordingToBehavior(handler)
        return Decimal(rounded)
    }

    actual fun movePointLeft(places: Int): Decimal {
        return Decimal(n.decimalNumberByMultiplyingByPowerOf10((-places).toShort()))
    }

    actual fun movePointRight(places: Int): Decimal {
        return Decimal(n.decimalNumberByMultiplyingByPowerOf10(places.toShort()))
    }

    /**
     * Set scale (number of fractional digits) and rounding.
     *
     * Internally uses NSDecimalNumberHandler to round the value to `scale` digits.
     */
    actual fun setScale(scale: Int, rounding: Rounding): Decimal {
        val handler = createHandler(scale, rounding)
        val rounded = n.decimalNumberByRoundingAccordingToBehavior(handler)
        return Decimal(rounded)
    }

    actual fun toPlainString(): String {
        // NSDecimalNumber.stringValue gives a normalized decimal string without exponent for typical numbers.
        return n.stringValue
    }

    actual fun toIntegerString(): String {
        // Round to 0 fractional digits using HALF_UP and return the integer as string (no decimal point).
        val rounded = setScale(0, Rounding.HALF_UP)
        // Use stringValue; it will be like "123" or "-42"
        return rounded.n.stringValue
    }

    actual fun abs(): Decimal {
        // Compare to NotANumber separately
        if (isNan()) return this
        val doubleVal = n.doubleValue
        val positive = if (doubleVal < 0.0) {
            // create positive via absolute value string
            val s = n.stringValue
            if (s.startsWith("-")) Decimal(NSDecimalNumber(string = s.removePrefix("-"))) else this
        } else this
        return positive
    }

    actual fun isNan(): Boolean {
        // NSDecimalNumber.notANumber is a special singleton; compare using isEqualToNumber or check string
        val notANumber = NSDecimalNumber.notANumber
        return n == notANumber || n.stringValue == notANumber.stringValue
    }

    /**
     * Create an NSDecimalNumberHandler with the given scale and rounding mode.
     *
     * - scale must be provided as Short on Kotlin/Native bindings (hence toShort()).
     * - rounding mapping uses NSRoundPlain / NSRoundDown / NSRoundUp which are the
     *   Kotlin/Native mappings of the underlying NSRoundingMode enum.
     */
    private fun createHandler(scale: Int, rounding: Rounding): NSDecimalNumberHandler {
        val nativeRounding = when (rounding) {
            Rounding.HALF_UP -> NSRoundingMode.NSRoundPlain   // Round to nearest; .5 -> away from zero
            Rounding.DOWN -> NSRoundingMode.NSRoundDown      // Truncate toward zero
            Rounding.UP -> NSRoundingMode.NSRoundUp          // Always round away from zero
        }
        // NSDecimalNumberHandler expects Short for the scale parameter in Kotlin/Native,
        // so convert safely to Short (users should use reasonable scale values).
        return NSDecimalNumberHandler(
            roundingMode = nativeRounding,
            scale = scale.toShort(),
            raiseOnExactness = false,
            raiseOnOverflow = false,
            raiseOnUnderflow = false,
            raiseOnDivideByZero = false
        )
    }

    /**
     * Returns true if the given NSDecimalNumber represents zero or is NaN.
     * Uses compare(...) to handle different representations like "0", "0.0", etc.
     */
    private fun isZeroOrNaN(x: NSDecimalNumber): Boolean {
        // Check NaN first
        if (x == NSDecimalNumber.notANumber()) return true
        // compare(...) returns 0L when equal
        val cmp = x.compare(NSDecimalNumber.zero)
        return cmp == 0L
    }
}
