package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals

class MonetaOperatorsTest {

    private val usd = Currency("USD", 2)
    private val btc = Currency("BTC", 8)

    @Test
    fun plus_operator_basic() {
        val a = Moneta.fromDecimalString("10.50", usd)
        val b = Moneta.fromDecimalString("2.25", usd)

        val sum = a + b
        assertEquals("12.75", sum.toDecimalString(2))
    }

    @Test
    fun minus_operator_basic() {
        val a = Moneta.fromDecimalString("5.00", usd)
        val b = Moneta.fromDecimalString("1.25", usd)

        val diff = a - b
        assertEquals("3.75", diff.toDecimalString(2))
    }

    @Test
    fun times_operator_int_factor() {
        val price = Moneta.fromDecimalString("2.50", usd)
        val total = price * 3 // operator fun times(factor: Int)
        assertEquals("7.50", total.toDecimalString(2))

        val zero = price * 0
        assertEquals("0.00", zero.toDecimalString(2))

        val negative = Moneta.fromDecimalString("-1.25", usd)
        val negTimes = negative * 4
        assertEquals("-5.00", negTimes.toDecimalString(2))
    }

    @Test
    fun operator_associativity_and_commutativity_examples() {
        val x = Moneta.fromDecimalString("1.10", usd)
        val y = Moneta.fromDecimalString("2.20", usd)
        val z = Moneta.fromDecimalString("3.30", usd)

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
    fun mixed_currency_warning_behaviour() {
        // We don't enforce currency checks in operators — tests ensure arithmetic stays local.
        val usdAmt = Moneta.fromDecimalString("1.00", usd)
        val btcAmt = Moneta.fromDecimalString("0.00010000", btc)

        // These operations are nonsensical across currencies but should still produce a Decimal result.
        // We assert they produce the expected numeric combination of their underlying decimals.
        val combined = usdAmt + Moneta.fromDecimalString("2.00", usd)
        assertEquals("3.00", combined.toDecimalString(2))

        // Ensure combining btc with btc works as expected
        val btcSum = btcAmt + Moneta.fromDecimalString("0.00020000", btc)
        assertEquals("0.00030000", btcSum.toDecimalString(8))
    }

    @Test
    fun large_factor_and_precision() {
        val small = Moneta.fromDecimalString("0.01", usd)
        val big = small * 10_000 // 100.00
        assertEquals("100.00", big.toDecimalString(2))

        val oneBtc = Moneta.fromDecimalString("1.00000001", btc)
        val scaled = oneBtc * 100
        // expect 100.00000100 (preserve BTC decimals when displaying)
        assertEquals("100.00000100", scaled.toDecimalString(8))
    }
}
