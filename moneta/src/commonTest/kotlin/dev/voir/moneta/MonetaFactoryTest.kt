package dev.voir.moneta

import dev.voir.decimal.Decimal
import dev.voir.decimal.Rounding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MonetaFactoryTest {
    @Test
    fun `integer factories create whole units`() {
        assertEquals("5.00", Moneta.fromInt(5, USD).toDecimalString(2))
        assertEquals("123.00", Moneta.fromLong(123L, USD).toDecimalString(2))
        assertEquals("2.00", Moneta.fromShort(2, USD).toDecimalString(2))
        assertEquals("1.00", Moneta.fromByte(1, USD).toDecimalString(2))
    }

    @Test
    fun `integer factories accept extreme values`() {
        assertEquals("9223372036854775807", Moneta.fromLong(Long.MAX_VALUE, JPY).toDecimalString())
        assertEquals("9223372036854775808", Moneta.fromLong(Long.MIN_VALUE, JPY).toDecimalString())
        assertEquals("2147483648", Moneta.fromInt(Int.MIN_VALUE, JPY).toDecimalString())
    }

    @Test
    fun `decimal string rounds half up to currency scale by default`() {
        assertEquals("1.24", money("1.235").toDecimalString())
    }

    @Test
    fun `decimal string honours explicit rounding`() {
        assertEquals("1.23", Moneta.fromDecimalString("1.239", USD, Rounding.DOWN).toDecimalString())
        assertEquals("1.24", Moneta.fromDecimalString("1.231", USD, Rounding.UP).toDecimalString())
        assertEquals("1.23", Moneta.fromDecimalString("1.231", USD, Rounding.FLOOR).toDecimalString())
        assertEquals("1.24", Moneta.fromDecimalString("1.231", USD, Rounding.CEILING).toDecimalString())
        assertEquals("1.22", Moneta.fromDecimalString("1.225", USD, Rounding.HALF_EVEN).toDecimalString())
        assertEquals("1.24", Moneta.fromDecimalString("1.235", USD, Rounding.HALF_EVEN).toDecimalString())
    }

    @Test
    fun `negative input is stored as absolute value`() {
        assertEquals(money("3.50"), money("-3.50"))
        assertEquals(money("3.00"), Moneta.fromInt(-3, USD))
        assertEquals(money("0.50"), Moneta.fromDouble(-0.5, USD))
    }

    @Test
    fun `negative input rounds by magnitude`() {
        // FLOOR on -1.231 would round away from zero; the magnitude 1.231 rounds down instead.
        assertEquals("1.23", Moneta.fromDecimalString("-1.231", USD, Rounding.FLOOR).toDecimalString())
    }

    @Test
    fun `invalid decimal text is rejected`() {
        assertFailsWith<IllegalArgumentException> { Moneta.fromDecimalString("abc", USD) }
        assertFailsWith<IllegalArgumentException> { Moneta.fromDecimalString("1,000.00", USD) }
        assertFailsWith<IllegalArgumentException> { Moneta.fromDecimalString("1e3", USD) }
        assertFailsWith<IllegalArgumentException> { Moneta.fromDecimalString("", USD) }
    }

    @Test
    fun `decimal factory rounds to currency scale`() {
        assertEquals("0.33", Moneta.fromDecimal(Decimal.parse("0.333"), USD).toDecimalString())
    }

    @Test
    fun `double keeps its printed digits`() {
        assertEquals("0.1", Moneta.fromDouble(0.1, BTC).toDecimalString())
        assertEquals("1.24", Moneta.fromDouble(1.235, USD).toDecimalString())
    }

    @Test
    fun `double in scientific notation is expanded`() {
        assertEquals("10000000", Moneta.fromDouble(1.0E7, USD).toDecimalString())
        assertEquals("0.0000001", Moneta.fromDouble(1.0E-7, BTC).toDecimalString())
    }

    @Test
    fun `float keeps its printed digits`() {
        assertEquals("0.1", Moneta.fromFloat(0.1f, BTC).toDecimalString())
        assertEquals("0.125", Moneta.fromFloat(0.125f, BTC).toDecimalString())
    }

    @Test
    fun `float in scientific notation is expanded`() {
        assertEquals("10000000000", Moneta.fromFloat(1.0E10f, USD).toDecimalString())
        assertEquals("0.00001", Moneta.fromFloat(1.0E-5f, BTC).toDecimalString())
    }

    @Test
    fun `non-finite floating values are rejected`() {
        assertFailsWith<IllegalArgumentException> { Moneta.fromDouble(Double.NaN, USD) }
        assertFailsWith<IllegalArgumentException> { Moneta.fromDouble(Double.POSITIVE_INFINITY, USD) }
        assertFailsWith<IllegalArgumentException> { Moneta.fromFloat(Float.NaN, USD) }
        assertFailsWith<IllegalArgumentException> { Moneta.fromFloat(Float.NEGATIVE_INFINITY, USD) }
    }

    @Test
    fun `number dispatches to the matching primitive factory`() {
        val numbers: List<Number> = listOf(7, 7L, 7.toShort(), 7.toByte(), 7.0, 7.0f)
        numbers.forEach { assertEquals(money("7"), Moneta.fromNumber(it, USD), "for $it") }
    }

    @Test
    fun `atomic factories place the decimal point by currency decimals`() {
        assertEquals("1.5", Moneta.fromAtomicInt(150, USD).toDecimalString())
        assertEquals("0.12345678", Moneta.fromAtomicLong(12_345_678L, BTC).toDecimalString())
        assertEquals("1", Moneta.fromAtomicString("1000000000000000000", ETH).toDecimalString())
        assertEquals("0.000000000000000001", Moneta.fromAtomicString("1", ETH).toDecimalString())
        assertEquals("150", Moneta.fromAtomicInt(150, JPY).toDecimalString())
    }

    @Test
    fun `atomic factories store absolute value`() {
        assertEquals(money("1.50"), Moneta.fromAtomicLong(-150L, USD))
        assertEquals(money("1.50"), Moneta.fromAtomicString("-150", USD))
    }

    @Test
    fun `atomic text must be an integer`() {
        assertFailsWith<IllegalArgumentException> { Moneta.fromAtomicString("12.5", USD) }
        assertFailsWith<IllegalArgumentException> { Moneta.fromAtomicString("abc", USD) }
    }

    @Test
    fun `zero uses the requested currency`() {
        val zero = Moneta.zero(BTC)

        assertEquals(BTC, zero.currency)
        assertEquals(true, zero.isZero())
        assertEquals(Currency(), Moneta.zero().currency)
    }
}
