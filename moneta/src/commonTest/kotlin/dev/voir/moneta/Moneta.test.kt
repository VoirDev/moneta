package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MonetaTest {
    private val usd = Currency("USD", 2)
    private val btc = Currency("BTC", 8)
    private val eth = Currency("ETH", 18)

    @Test
    fun constructFromIntegers_wholeUnits() {
        val m1 = Moneta.fromInt(5, usd)        // 5 -> 5.00
        assertEquals("5.00", m1.toDecimalString(2))

        val m2 = Moneta.fromLong(123L, usd)
        assertEquals("123.00", m2.toDecimalString(2))

        val m3 = Moneta.fromShort(2, usd)
        assertEquals("2.00", m3.toDecimalString(2))

        val m4 = Moneta.fromByte(1, usd)
        assertEquals("1.00", m4.toDecimalString(2))
    }

    @Test
    fun constructFromAtomicValues() {
        // 150 cents -> $1.50
        val a = Moneta.fromAtomicInt(150, usd)
        assertEquals("1.50", a.toDecimalString(2))

        val b = Moneta.fromAtomicLong(12345678L, btc) // 0.12345678 BTC
        assertEquals("0.12345678", b.toDecimalString(8))

        val c = Moneta.fromAtomicString("1000000000000000000", eth) // 1 ETH in wei
        assertEquals(
            "1.00",
            c.toDecimalString(2)
        ) // note: moved then rounded to 2 for display request
        // but ensure precise ETH decimal output when requesting ETH decimals:
        assertEquals("1.000000000000000000", c.toDecimalString(18))
    }

    @Test
    fun constructFromDecimalStrings_and_fromDecimalString() {
        val m = Moneta.fromDecimalString("1.2345", usd)
        // scaled to USD decimals (2) at construction (HALF_UP default)
        assertEquals("1.23", m.toDecimalString(2))

        // using explicit fromDecimalString which also sets scale to currency.decimals
        val btc = Moneta.fromDecimalString("0.00000012", btc)
        assertEquals("0.00000012", btc.toDecimalString(8))

        // invalid decimal text should throw
        assertFailsWith<IllegalArgumentException> {
            Moneta.fromDecimalString("abc", usd)
        }
    }

    @Test
    fun constructFromDoubleAndFloat() {
        val d = Moneta.fromDouble(1.5, usd)
        // fromDouble uses Decimal.of(value.toString()) then setScale to currency.decimals
        assertEquals("1.50", d.toDecimalString(2))

        val f = Moneta.fromFloat(0.125f, btc)
        // float -> string conversion may produce "0.125" -> scaled to BTC decimals (8)
        // so ensure at least significant digits preserved
        assertTrue(f.toDecimalString().startsWith("0.125"))
    }

    @Test
    fun arithmetic_plus_minus_times_divide() {
        val a = Moneta.fromDecimalString("10.00", usd)
        val b = Moneta.fromDecimalString("2.50", usd)

        val sum = a.plus(b)
        assertEquals("12.50", sum.toDecimalString(2))

        val diff = a.minus(b)
        assertEquals("7.50", diff.toDecimalString(2))

        val prod = b.times(3) // 2.50 * 3 = 7.50
        assertEquals("7.50", prod.toDecimalString(2))

        // divide by integer
        val div = a.divide(4, scale = 4) // 10.00 / 4 = 2.5
        // request 2 decimals for human display
        assertEquals("2.50", div.toDecimalString(2))
    }

    @Test
    fun toAtomicString_and_rounding_behavior() {
        // HALF_UP rounding: 1.235 -> 1.24 -> atomic 124 cents
        val m = Moneta.fromDecimalString("1.235", usd, rounding = Rounding.HALF_UP)
        val atomic = m.toAtomicString(usd, rounding = Rounding.HALF_UP)
        assertEquals("124", atomic)

        // DOWN rounding: 1.239 -> 1.23 -> atomic 123
        val m2 = Moneta.fromDecimalString("1.239", usd, rounding = Rounding.DOWN)
        val atomic2 = m2.toAtomicString(usd, rounding = Rounding.DOWN)
        assertEquals("123", atomic2)
    }

    @Test
    fun negative_values_and_zero() {
        val neg = Moneta.fromDecimalString("-3.50", usd)
        assertEquals("-3.50", neg.toDecimalString(2))

        val zero = Moneta.zero()
        assertEquals("0", zero.toDecimalString()) // plain string shows "0"
        // atomic zero for USD
        assertEquals("0", zero.toAtomicString(usd))
    }

    @Test
    fun fromNumber_generic_dispatch() {
        val n1: Number = 7
        val m1 = Moneta.fromNumber(n1, usd)
        assertEquals("7.00", m1.toDecimalString(2))

        val n2: Number = 0.125
        val m2 = Moneta.fromNumber(n2, btc)
        // check that numeric string preserved at least some fractional digits
        assertTrue(m2.toDecimalString().startsWith("0.125"))
    }

    @Test
    fun big_currency_decimals_eth_example() {
        val ethVal = Moneta.fromDecimalString("0.5", eth)
        // ensure internal decimal precision supports 18 decimals when requested
        assertEquals("0.500000000000000000", ethVal.toDecimalString(18))

        // converting atomic string of wei back to decimal
        val oneWei = Moneta.fromAtomicString("1", eth)
        assertEquals("0.000000000000000001", oneWei.toDecimalString(18))
    }
}
