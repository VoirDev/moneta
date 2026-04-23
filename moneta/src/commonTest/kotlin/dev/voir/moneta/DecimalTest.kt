package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class DecimalTest {

    @Test
    fun constructAndToPlainString() {
        val a = Decimal.of("123.4500")
        assertEquals("123.45", a.toPlainString(), "toPlainString should trim trailing zeros")
        val b = Decimal.of("-0.00100")
        assertEquals("-0.001", b.toPlainString())
        val c = Decimal.ofInteger("1000")
        assertEquals("1000", c.toPlainString())
    }

    @Test
    fun addSubtractMultiply() {
        val a = Decimal.of("1.5")
        val b = Decimal.of("2.25")
        val sum = a.add(b)
        assertEquals("3.75", sum.toPlainString())

        val diff = b.subtract(a)
        assertEquals("0.75", diff.toPlainString())

        val prod = a.multiply(b)
        // 1.5 * 2.25 = 3.375
        assertEquals("3.375", prod.toPlainString())
    }

    @Test
    fun multiplyByIntegerString() {
        val d = Decimal.of("1.234")
        val r = d.multiply("1000")
        assertEquals("1234", r.toPlainString())
    }

    @Test
    fun divideWithRoundingHalfUp() {
        val a = Decimal.of("1")
        val b = Decimal.of("3")
        val result = a.divide(b, 6, Rounding.HALF_UP)
        // 1/3 ≈ 0.333333, half up to 6 digits
        assertEquals("0.333333", result.toPlainString())
    }

    @Test
    fun divideRoundingDownAndUp() {
        val a = Decimal.of("10")
        val b = Decimal.of("3")
        val down = a.divide(b, 2, Rounding.DOWN) // 10/3 = 3.333... -> 3.33
        val up = a.divide(b, 2, Rounding.UP)     // -> 3.34
        assertEquals("3.33", down.toPlainString())
        assertEquals("3.34", up.toPlainString())
    }

    @Test
    fun movePointLeftRight() {
        val a = Decimal.ofInteger("12345")
        val left = a.movePointLeft(2)  // -> 123.45
        assertEquals("123.45", left.toPlainString())

        val b = Decimal.of("1.23")
        val right = b.movePointRight(3) // -> 1230
        assertEquals("1230", right.toPlainString())
    }

    @Test
    fun setScaleRounding() {
        val a = Decimal.of("3.14159")
        val scaled = a.setScale(2, Rounding.HALF_UP) // 3.14
        assertEquals("3.14", scaled.toPlainString())

        val b = Decimal.of("2.345")
        assertEquals("2.34", b.setScale(2, Rounding.DOWN).toPlainString())
        assertEquals("2.35", b.setScale(2, Rounding.UP).toPlainString())

        val c = Decimal.of("5")
        assertEquals("5", c.setScale(2, Rounding.DOWN).toPlainString())
    }

    @Test
    fun toIntegerString_roundsHalfUp() {
        val a = Decimal.of("1.6")
        assertEquals("2", a.toIntegerString())
        val b = Decimal.of("1.49")
        assertEquals("1", b.toIntegerString())
    }

    @Test
    fun divideByZeroThrows() {
        val a = Decimal.of("1")
        val zero = Decimal.of("0")
        assertFailsWith<ArithmeticException> {
            // platform implementations should throw on divide by zero
            a.divide(zero, 2, Rounding.HALF_UP)
        }
    }

    @Test
    fun invalidInputThrows() {
        assertFailsWith<IllegalArgumentException> { Decimal.of("") }
        assertFailsWith<IllegalArgumentException> { Decimal.of("abc") }
        assertFailsWith<IllegalArgumentException> { Decimal.ofInteger("") }
        assertFailsWith<IllegalArgumentException> { Decimal.ofInteger("1.2") }
    }


    @Test
    fun abs_positiveRemainsPositive() {
        val d = Decimal.of("1.23")
        val a = d.abs()
        assertEquals("1.23", a.toPlainString(), "abs() of a positive value should be identical")
    }

    @Test
    fun abs_negativeBecomesPositive() {
        val d = Decimal.of("-1.23")
        val a = d.abs()
        assertEquals("1.23", a.toPlainString(), "abs() should remove the negative sign")
    }

    @Test
    fun abs_zeroRemainsZero() {
        val z = Decimal.zero()
        val a = z.abs()
        assertEquals("0", a.toPlainString(), "abs() of zero should be zero")
    }

    @Test
    fun abs_preservesMagnitude_after_movePointLeftAndRight() {
        // sanity test for scaled values (e.g., atomic → decimal → abs)
        val atomic = Decimal.ofInteger("12345").movePointLeft(2) // 123.45
        val a = atomic.abs()
        assertEquals("123.45", a.toPlainString())
    }

    @Test
    fun bigDecimalHasNoNaN() {
        // On JVM BigDecimal cannot represent NaN — isNan must always be false.
        val zero = Decimal.of("0")
        val negative = Decimal.of("-123.45")
        val fractional = Decimal.of("3.14159")

        assertFalse(
            zero.isNan(),
            "BigDecimal-backed Decimal must not report NaN for 0"
        )
        assertFalse(
            negative.isNan(),
            "BigDecimal-backed Decimal must not report NaN for negative values"
        )
        assertFalse(
            fractional.isNan(),
            "BigDecimal-backed Decimal must not report NaN for fractional values"
        )
    }
}
