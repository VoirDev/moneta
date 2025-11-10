package dev.voir.moneta

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

actual class Decimal private constructor(private val bd: BigDecimal) {
    actual companion object {
        private val UNLIMITED = MathContext.UNLIMITED

        // Accepts optional sign, integer part (required) and optional fractional part
        private val DECIMAL_RE = Regex("""^[+-]?\d+(\.\d+)?$""")
        private val INTEGER_RE = Regex("""^[+-]?\d+$""")

        actual fun of(decimalString: String): Decimal {
            val s = decimalString.trim()
            if (s.isEmpty()) throw IllegalArgumentException("Empty decimal string")
            if (!DECIMAL_RE.matches(s)) throw IllegalArgumentException("Invalid decimal string: $decimalString")

            val v = try {
                BigDecimal(s, UNLIMITED)
            } catch (e: Exception) {
                // Shouldn't normally happen because of the regex check, but guard anyway
                throw IllegalArgumentException("Invalid decimal string: $decimalString", e)
            }

            return Decimal(v)
        }

        actual fun ofInteger(integerString: String): Decimal {
            val s = integerString.trim()
            if (s.isEmpty()) throw IllegalArgumentException("Empty integer string")

            if (!INTEGER_RE.matches(s)) throw IllegalArgumentException("Invalid integer string: $integerString")

            val v = try {
                BigDecimal(s, UNLIMITED)
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid integer string: $integerString", e)
            }

            return Decimal(v)
        }

        actual fun zero(): Decimal = Decimal(BigDecimal.ZERO)
    }

    actual fun add(other: Decimal): Decimal = Decimal(bd.add(other.bd))
    actual fun subtract(other: Decimal): Decimal = Decimal(bd.subtract(other.bd))
    actual fun multiply(other: Decimal): Decimal = Decimal(bd.multiply(other.bd))
    actual fun multiply(integerStr: String): Decimal {
        if (integerStr.trim().isEmpty()) throw IllegalArgumentException("Empty integer multiplier")
        if (!INTEGER_RE.matches(integerStr)) throw IllegalArgumentException("Invalid integer multiplier: $integerStr")
        return Decimal(bd.multiply(BigDecimal(integerStr)))
    }

    actual fun divide(other: Decimal, scale: Int, rounding: Rounding): Decimal {
        // BigDecimal.divide will throw ArithmeticException on divide-by-zero; let it propagate
        val mode = rounding.toRoundingMode()
        return Decimal(bd.divide(other.bd, scale, mode))
    }

    actual fun divide(integerStr: String, scale: Int, rounding: Rounding): Decimal =
        divide(ofInteger(integerStr), scale, rounding)

    actual fun movePointLeft(places: Int): Decimal = Decimal(bd.movePointLeft(places))

    actual fun movePointRight(places: Int): Decimal = Decimal(bd.movePointRight(places))

    actual fun setScale(scale: Int, rounding: Rounding): Decimal =
        Decimal(bd.setScale(scale, rounding.toRoundingMode()))

    actual fun toPlainString(): String = bd.stripTrailingZeros().toPlainString()

    actual fun toIntegerString(): String =
        bd.setScale(0, RoundingMode.HALF_UP).toBigIntegerExact().toString()

    actual fun abs(): Decimal = Decimal(bd.abs())

    actual fun isNan(): Boolean = false // BigDecimal has no NaN

    private fun Rounding.toRoundingMode(): RoundingMode = when (this) {
        Rounding.HALF_UP -> RoundingMode.HALF_UP
        Rounding.DOWN -> RoundingMode.DOWN
        Rounding.UP -> RoundingMode.UP
    }
}
