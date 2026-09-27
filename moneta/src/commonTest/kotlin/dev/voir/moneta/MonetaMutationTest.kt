package dev.voir.moneta

import dev.voir.decimal.Decimal
import dev.voir.decimal.Rounding
import kotlin.test.Test
import kotlin.test.assertEquals

class MonetaMutationTest {
    @Test
    fun `withCurrency re-labels the amount with more decimals`() {
        val btcValue = money("1.23").withCurrency(BTC)

        assertEquals(BTC, btcValue.currency)
        assertEquals("1.23000000", btcValue.toDecimalString(8))
        assertEquals(123_000_000L, btcValue.toAtomicLong())
    }

    @Test
    fun `withCurrency rounds to fewer decimals`() {
        assertEquals(money("1.23"), money("1.23456789", BTC).withCurrency(USD))
        assertEquals(Moneta.fromInt(1, JPY), money("1.49").withCurrency(JPY))
        assertEquals(Moneta.fromInt(1, JPY), money("1.50").withCurrency(JPY, Rounding.DOWN))
    }

    @Test
    fun `withValue replaces the amount and keeps currency`() {
        val value = Moneta.zero(USD).withValue(Decimal.parse("9.999"))

        assertEquals(money("10.00"), value)
    }

    @Test
    fun `withValue stores absolute value`() {
        assertEquals(money("5"), Moneta.zero(USD).withValue(Decimal.parse("-5")))
    }

    @Test
    fun `withDecimalString parses text in the same currency`() {
        val value = Moneta.zero(USD).withDecimalString("2.40")

        assertEquals(money("2.4"), value)
        assertEquals("2.4$", value.toFormattedString(appendSymbol = true))
    }

    @Test
    fun `withAtomicLong replaces the amount from atomic units`() {
        assertEquals(money("12.34"), Moneta.zero(USD).withAtomicLong(1234L))
    }
}
