package dev.voir.moneta

import dev.voir.decimal.Decimal
import kotlin.test.Test
import kotlin.test.assertEquals

class MonetaMutationTest {

    private val usd = Currency(code = "USD", decimals = 2, symbol = "$")
    private val btc = Currency(code = "BTC", decimals = 8, symbol = "₿")

    @Test
    fun `withCurrency re-applies same value with new precision`() {
        val usdValue = Moneta.fromDecimalString("1.23", usd)
        val btcValue = usdValue.withCurrency(btc)

        assertEquals("1.23$", usdValue.toFormattedString(appendSymbol = true))
        assertEquals("1.23₿", btcValue.toFormattedString(appendSymbol = true))
        assertEquals("1.23000000", btcValue.toDecimalString(scale = 8))
        assertEquals(123L, usdValue.toAtomicLongOrNull())
        assertEquals(123000000L, btcValue.toAtomicLongOrNull())
    }

    @Test
    fun `withValue replaces decimal amount and keeps currency`() {
        val value = Moneta.zero()
            .withCurrency(usd)
            .withValue(Decimal.of("9.90"))

        assertEquals(usd, value.currency)
        assertEquals("9.9$", value.toFormattedString(appendSymbol = true))
        assertEquals(990L, value.toAtomicLongOrNull())
    }

    @Test
    fun `withDecimalString parses final calculator value`() {
        val value = Moneta.zero()
            .withCurrency(usd)
            .withDecimalString("2.40")

        assertEquals("2.4", value.toDecimalString())
        assertEquals("2.40", value.toDecimalString(scale = 2))
        assertEquals("2.4$", value.toFormattedString(appendSymbol = true))
    }

    @Test
    fun `withAtomicLong replaces amount from atomic units`() {
        val value = Moneta.zero()
            .withCurrency(usd)
            .withAtomicLong(1234L)

        assertEquals("12.34", value.toDecimalString())
        assertEquals(1234L, value.toAtomicLongOrNull())
    }
}
