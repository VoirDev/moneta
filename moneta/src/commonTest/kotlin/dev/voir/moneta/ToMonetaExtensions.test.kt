package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToMonetaExtensionsTest {
    @Test
    fun int_toMoney_wholeUnits() {
        val m: Moneta = 5.toMoneta(Currency(code = "usd", decimals = 2))
        // explicit scale 2 to assert formatting
        assertEquals("5.00", m.toDecimalString(2))
    }

    @Test
    fun long_toMoney_wholeUnits() {
        val m = 123L.toMoneta(Currency(code = "usd", decimals = 2))
        assertEquals("123.00", m.toDecimalString(2))
    }

    @Test
    fun short_and_byte_toMoney() {
        val s: Short = 2
        val b: Byte = 1
        assertEquals("2.00", s.toMoneta(Currency(code = "usd", decimals = 2)).toDecimalString(2))
        assertEquals("1.00", b.toMoneta(Currency(code = "usd", decimals = 2)).toDecimalString(2))
    }

    @Test
    fun double_toMoney_and_rounding_default() {
        // Double -> parsed from toString(), then scaled to currency.decimals (2)
        val d = 1.235.toMoneta(
            Currency(
                code = "usd",
                decimals = 2
            )
        ) // default HALF_UP rounding in factory
        // 1.235 -> scaled HALF_UP -> 1.24
        assertEquals("1.24", d.toDecimalString(2))

        // negative double
        val dn = (-0.5).toMoneta(Currency(code = "usd", decimals = 2))
        assertEquals("0.50", dn.toDecimalString(2))
    }

    @Test
    fun float_toMoney_basic_behavior() {
        val f = 0.125f.toMoneta(
            Currency(
                code = "btc",
                decimals = 8
            )
        ) // float -> "0.125" -> scaled for BTC decimals (8)
        // ensure string begins with expected significant digits
        assertTrue(f.toDecimalString().startsWith("0.125"))
    }

    @Test
    fun number_toMoney_dispatches_correctly() {
        val n1: Number = 7                     // boxed Int
        val m1 = n1.toMoneta(Currency(code = "usd", decimals = 2))
        assertEquals("7.00", m1.toDecimalString(2))

        val n2: Number = 0.125                 // Double
        val m2 = n2.toMoneta(Currency(code = "btc", decimals = 8))
        // decimal-preservation: verify that a few leading sig digits are kept
        assertTrue(m2.toDecimalString().startsWith("0.125"))
    }

    @Test
    fun explicit_rounding_parameter_respected() {
        // create using the extension with rounding DOWN explicitly
        val mDown = 1.239.toMoneta(Currency(code = "usd", decimals = 2), rounding = Rounding.DOWN)
        assertEquals("1.23", mDown.toDecimalString(2))

        val mUp = 1.231.toMoneta(Currency(code = "usd", decimals = 2), rounding = Rounding.UP)
        assertEquals(
            "1.24",
            mUp.toDecimalString(2)
        ) // UP increases magnitude when fractional part present
    }

    @Test
    fun zero_and_negative_integers() {
        val z = 0.toMoneta(Currency(code = "usd", decimals = 2))
        assertEquals("0.00", z.toDecimalString(2))

        val neg = (-3).toMoneta(Currency(code = "usd", decimals = 2))
        assertEquals("3.00", neg.toDecimalString(2))
    }

    @Test
    fun large_values_and_btc_precision() {
        val oneBtc = 1.toMoneta(Currency(code = "btc", decimals = 8))
        assertEquals("1.00000000", oneBtc.toDecimalString(8))

        val big = 9_999_999_999L.toMoneta(Currency(code = "usd", decimals = 2))
        // just assert whole-unit formatting with 2 decimals
        assertTrue(big.toDecimalString(2).endsWith(".00"))
    }
}
