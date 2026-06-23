package dev.voir.moneta

import dev.voir.decimal.Rounding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToMonetaExtensionsTest {
    @Test
    fun `convert Int to Moneta whole units`() {
        val m: Moneta = 5.toMoneta(Currency(code = "usd", decimals = 2))
        // explicit scale 2 to assert formatting
        assertEquals("5.00", m.toDecimalString(2))
    }

    @Test
    fun `convert Long to Moneta whole units`() {
        val m = 123L.toMoneta(Currency(code = "usd", decimals = 2))
        assertEquals("123.00", m.toDecimalString(2))
    }

    @Test
    fun `convert Short and Byte to Moneta whole units`() {
        val s: Short = 2
        val b: Byte = 1
        assertEquals("2.00", s.toMoneta(Currency(code = "usd", decimals = 2)).toDecimalString(2))
        assertEquals("1.00", b.toMoneta(Currency(code = "usd", decimals = 2)).toDecimalString(2))
    }

    @Test
    fun `convert Double to Moneta with default rounding`() {
        // Double -> Decimal.fromDouble, then scaled to currency.decimals (2)
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
    fun `convert Float to Moneta`() {
        val f = 0.125f.toMoneta(
            Currency(
                code = "btc",
                decimals = 8
            )
        ) // Float -> string form -> scaled for BTC decimals (8)
        // ensure string begins with expected significant digits
        assertTrue(f.toDecimalString().startsWith("0.125"))
    }

    @Test
    fun `dispatch Number to Moneta correctly`() {
        val n1: Number = 7                     // boxed Int
        val m1 = n1.toMoneta(Currency(code = "usd", decimals = 2))
        assertEquals("7.00", m1.toDecimalString(2))

        val n2: Number = 0.125                 // Double
        val m2 = n2.toMoneta(Currency(code = "btc", decimals = 8))
        // decimal-preservation: verify that a few leading sig digits are kept
        assertTrue(m2.toDecimalString().startsWith("0.125"))
    }

    @Test
    fun `respect explicit rounding parameter`() {
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
    fun `convert zero and negative integers`() {
        val z = 0.toMoneta(Currency(code = "usd", decimals = 2))
        assertEquals("0.00", z.toDecimalString(2))

        val neg = (-3).toMoneta(Currency(code = "usd", decimals = 2))
        assertEquals("3.00", neg.toDecimalString(2))
    }

    @Test
    fun `convert large values and preserve BTC precision`() {
        val oneBtc = 1.toMoneta(Currency(code = "btc", decimals = 8))
        assertEquals("1.00000000", oneBtc.toDecimalString(8))

        val big = 9_999_999_999L.toMoneta(Currency(code = "usd", decimals = 2))
        // just assert whole-unit formatting with 2 decimals
        assertTrue(big.toDecimalString(2).endsWith(".00"))
    }
}
