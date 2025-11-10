package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToMonetaExtensionsTest {

    private val usd = Currency("USD", 2)
    private val btc = Currency("BTC", 8)

    @Test
    fun int_toMoney_wholeUnits() {
        val m: Moneta = 5.toMoney(usd)
        // explicit scale 2 to assert formatting
        assertEquals("5.00", m.toDecimalString(2))
    }

    @Test
    fun long_toMoney_wholeUnits() {
        val m = 123L.toMoney(usd)
        assertEquals("123.00", m.toDecimalString(2))
    }

    @Test
    fun short_and_byte_toMoney() {
        val s: Short = 2
        val b: Byte = 1
        assertEquals("2.00", s.toMoney(usd).toDecimalString(2))
        assertEquals("1.00", b.toMoney(usd).toDecimalString(2))
    }

    @Test
    fun double_toMoney_and_rounding_default() {
        // Double -> parsed from toString(), then scaled to currency.decimals (2)
        val d = 1.235.toMoney(usd) // default HALF_UP rounding in factory
        // 1.235 -> scaled HALF_UP -> 1.24
        assertEquals("1.24", d.toDecimalString(2))

        // negative double
        val dn = (-0.5).toMoney(usd)
        assertEquals("-0.50", dn.toDecimalString(2))
    }

    @Test
    fun float_toMoney_basic_behavior() {
        val f = 0.125f.toMoney(btc) // float -> "0.125" -> scaled for BTC decimals (8)
        // ensure string begins with expected significant digits
        assertTrue(f.toDecimalString().startsWith("0.125"))
    }

    @Test
    fun number_toMoney_dispatches_correctly() {
        val n1: Number = 7                     // boxed Int
        val m1 = n1.toMoney(usd)
        assertEquals("7.00", m1.toDecimalString(2))

        val n2: Number = 0.125                 // Double
        val m2 = n2.toMoney(btc)
        // decimal-preservation: verify that a few leading sig digits are kept
        assertTrue(m2.toDecimalString().startsWith("0.125"))
    }

    @Test
    fun explicit_rounding_parameter_respected() {
        // create using the extension with rounding DOWN explicitly
        val mDown = 1.239.toMoney(usd, rounding = Rounding.DOWN)
        assertEquals("1.23", mDown.toDecimalString(2))

        val mUp = 1.231.toMoney(usd, rounding = Rounding.UP)
        assertEquals(
            "1.24",
            mUp.toDecimalString(2)
        ) // UP increases magnitude when fractional part present
    }

    @Test
    fun zero_and_negative_integers() {
        val z = 0.toMoney(usd)
        assertEquals("0.00", z.toDecimalString(2))

        val neg = (-3).toMoney(usd)
        assertEquals("-3.00", neg.toDecimalString(2))
    }

    @Test
    fun large_values_and_btc_precision() {
        val oneBtc = 1.toMoney(btc)
        assertEquals("1.00000000", oneBtc.toDecimalString(8))

        val big = 9_999_999_999L.toMoney(usd)
        // just assert whole-unit formatting with 2 decimals
        assertTrue(big.toDecimalString(2).endsWith(".00"))
    }
}
