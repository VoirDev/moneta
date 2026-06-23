package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals

class MonetaOperatorsTest {
    @Test
    fun `add amounts with plus operator`() {
        val a = Moneta.fromDecimalString("10.50", Currency(code = "usd", decimals = 2))
        val b = Moneta.fromDecimalString("2.25", Currency(code = "usd", decimals = 2))

        val sum = a + b
        assertEquals("12.75", sum.toDecimalString(2))
    }

    @Test
    fun `subtract amounts with minus operator`() {
        val a = Moneta.fromDecimalString("5.00", Currency(code = "usd", decimals = 2))
        val b = Moneta.fromDecimalString("1.25", Currency(code = "usd", decimals = 2))

        val diff = a - b
        assertEquals("3.75", diff.toDecimalString(2))
    }

    @Test
    fun `multiply amount by integer factor with times operator`() {
        val price = Moneta.fromDecimalString("2.50", Currency(code = "usd", decimals = 2))
        val total = price * 3 // operator fun times(factor: Int)
        assertEquals("7.50", total.toDecimalString(2))

        val zero = price * 0
        assertEquals("0.00", zero.toDecimalString(2))

        val negative = Moneta.fromDecimalString("-1.25", Currency(code = "usd", decimals = 2))
        val negTimes = negative * 4
        assertEquals("5.00", negTimes.toDecimalString(2))
    }

    @Test
    fun `preserve expected addition associativity and commutativity examples`() {
        val x = Moneta.fromDecimalString("1.10", Currency(code = "usd", decimals = 2))
        val y = Moneta.fromDecimalString("2.20", Currency(code = "usd", decimals = 2))
        val z = Moneta.fromDecimalString("3.30", Currency(code = "usd", decimals = 2))

        // (x + y) + z == x + (y + z)
        val left = (x + y) + z
        val right = x + (y + z)
        assertEquals(left.toDecimalString(2), right.toDecimalString(2))

        // commutativity of addition
        val sum1 = x + y
        val sum2 = y + x
        assertEquals(sum1.toDecimalString(2), sum2.toDecimalString(2))
    }

    @Test
    fun `allow mixed currency arithmetic and keep left currency`() {
        // We don't enforce currency checks in operators — tests ensure arithmetic stays local.
        val usd = Currency(code = "usd", decimals = 2)
        val btc = Currency(code = "btc", decimals = 8)
        val usdAmt = Moneta.fromDecimalString("1.00", usd)
        val btcAmt = Moneta.fromDecimalString("0.00010000", btc)

        // These operations are nonsensical across currencies but should still produce a Decimal result.
        val combined = usdAmt + btcAmt
        assertEquals(usd, combined.currency)
        assertEquals("1.00", combined.toDecimalString(2))
        assertEquals("1.00010000", combined.toDecimalString(8))
    }

    @Test
    fun `allow arithmetic to produce signed results`() {
        val usd = Currency(code = "usd", decimals = 2)
        val one = Moneta.fromDecimalString("1.00", usd)
        val two = Moneta.fromDecimalString("2.00", usd)

        assertEquals("-1.00", one.minus(two).toDecimalString(2))
        assertEquals("-2.00", one.times(-2).toDecimalString(2))
        assertEquals("-0.50", one.divide(-2).toDecimalString(2))
    }

    @Test
    fun `preserve precision with large factors`() {
        val small = Moneta.fromDecimalString("0.01", Currency(code = "usd", decimals = 2))
        val big = small * 10_000 // 100.00
        assertEquals("100.00", big.toDecimalString(2))

        val oneBtc = Moneta.fromDecimalString("1.00000001", Currency(code = "btc", decimals = 8))
        val scaled = oneBtc * 100
        // expect 100.00000100 (preserve BTC decimals when displaying)
        assertEquals("100.00000100", scaled.toDecimalString(8))
    }
}
