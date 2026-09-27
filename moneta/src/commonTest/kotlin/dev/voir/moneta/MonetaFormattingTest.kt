package dev.voir.moneta

import dev.voir.decimal.Rounding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MonetaFormattingTest {
    @Test
    fun `decimal string without scale trims trailing zeros`() {
        assertEquals("1.5", money("1.50").toDecimalString())
        assertEquals("0", Moneta.zero(USD).toDecimalString())
    }

    @Test
    fun `decimal string with scale pads and rounds`() {
        assertEquals("1.50", money("1.5").toDecimalString(2))
        assertEquals("1.500000000000000000", money("1.5", ETH).toDecimalString(18))
        assertEquals("2", money("1.5").toDecimalString(0))
        assertEquals("1", money("1.5").toDecimalString(0, Rounding.DOWN))
    }

    @Test
    fun `decimal string rejects negative scale`() {
        val error = assertFailsWith<IllegalArgumentException> { money("1").toDecimalString(-1) }
        assertEquals("scale must not be negative, but was -1.", error.message)
    }

    @Test
    fun `formatted string pads exact decimals`() {
        assertEquals("123 456.70", money("123456.7000").toFormattedString(decimals = 2))
        assertEquals("1 234.00", money("1234").toFormattedString(decimals = 2))
        assertEquals("0.00", Moneta.zero(USD).toFormattedString(decimals = 2))
    }

    @Test
    fun `formatted string rounds to fewer decimals`() {
        assertEquals("1", money("1.49").toFormattedString(decimals = 0))
        assertEquals("2", money("1.50").toFormattedString(decimals = 0))
        assertEquals("1", money("1.50").toFormattedString(decimals = 0, rounding = Rounding.DOWN))
    }

    @Test
    fun `formatted string trims up to currency decimals when decimals are null`() {
        assertEquals("0.0001", money("0.00010000", BTC).toFormattedString())
        assertEquals("0.0000123", money("0.00001230", BTC).toFormattedString())
        assertEquals("12 345.6", money("12345.60").toFormattedString())
    }

    @Test
    fun `whole values show one zero decimal by default`() {
        assertEquals("1 234.0", money("1234.00").toFormattedString())
        assertEquals("1 000 000.0", money("1000000").toFormattedString())
        assertEquals("0.0", Moneta.zero(USD).toFormattedString())
    }

    @Test
    fun `whole values hide decimals when showDecimalIfZero is false`() {
        assertEquals("1 234", money("1234.00").toFormattedString(showDecimalIfZero = false))
        assertEquals("0", Moneta.zero(USD).toFormattedString(showDecimalIfZero = false))
    }

    @Test
    fun `currencies without decimals never show a separator`() {
        assertEquals("1 000", Moneta.fromInt(1000, JPY).toFormattedString())
        assertEquals("1 000", Moneta.fromInt(1000, JPY).toFormattedString(decimals = 0))
    }

    @Test
    fun `formatted string uses custom separators`() {
        assertEquals("1.234,56", money("1234.56").toFormattedString(decimals = 2, groupSeparator = '.', decimalSeparator = ','))
        assertEquals("1,000,000.0", money("1000000").toFormattedString(groupSeparator = ','))
    }

    @Test
    fun `formatted string rejects equal separators`() {
        assertFailsWith<IllegalArgumentException> {
            money("1").toFormattedString(groupSeparator = '.', decimalSeparator = '.')
        }
    }

    @Test
    fun `formatted string groups very large amounts`() {
        assertEquals("123 456 789 012 345.0", money("123456789012345").toFormattedString())
    }

    @Test
    fun `formatted string appends symbol only when requested and present`() {
        assertEquals("1.23$", money("1.23").toFormattedString(appendSymbol = true))
        assertEquals("1.23", money("1.23").toFormattedString())
        assertEquals("1.23", Moneta.fromDecimalString("1.23", Currency()).toFormattedString(appendSymbol = true))
    }

    @Test
    fun `formatted string rejects decimals outside currency scale`() {
        val error = assertFailsWith<IllegalArgumentException> { money("1.23").toFormattedString(decimals = 3) }
        assertEquals(
            "decimals must be null or in 0..2 for Currency(decimals=2, code=USD, symbol=$), but was 3.",
            error.message,
        )
        assertFailsWith<IllegalArgumentException> { money("1.23").toFormattedString(decimals = -1) }
    }
}
