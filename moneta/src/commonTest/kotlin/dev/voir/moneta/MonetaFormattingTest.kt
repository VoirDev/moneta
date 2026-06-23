package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MonetaFormattingTest {
    @Test
    fun `format with default separators and trim when decimals are null`() {
        val m = Moneta.fromDecimalString("123456.7000", Currency(code = "usd", decimals = 2))
        // decimals = null -> significant digits up to currency.decimals => trims trailing zeros
        val s = m.toFormattedString(decimals = 2)
        assertEquals("123 456.70", s)
    }

    @Test
    fun `hide decimal for whole values when showDecimalIfZero is false`() {
        val m = Moneta.fromDecimalString("1234.00", Currency(code = "usd", decimals = 2))
        val s = m.toFormattedString(decimals = null, showDecimalIfZero = false)
        assertEquals("1 234", s)
    }

    @Test
    fun `show single decimal zero for whole values when showDecimalIfZero is true`() {
        val m = Moneta.fromDecimalString("1234.00", Currency(code = "usd", decimals = 2))
        val s = m.toFormattedString(decimals = null, showDecimalIfZero = true)
        // trims to no fraction then shows single '0'
        assertEquals("1 234.0", s)
    }

    @Test
    fun `pad exact decimals when decimals are specified`() {
        val m = Moneta.fromDecimalString("1234", Currency(code = "usd", decimals = 2))
        val s = m.toFormattedString(decimals = 2) // always show 2 decimals
        assertEquals("1 234.00", s)
    }

    @Test
    fun `format with custom group and decimal separators`() {
        val m = Moneta.fromDecimalString("1234.56", Currency(code = "usd", decimals = 2))
        val s = m.toFormattedString(decimals = 2, groupSeparator = '.', decimalSeparator = ',')
        assertEquals("1.234,56", s)
    }

    @Test
    fun `group large whole numbers`() {
        val m = Moneta.fromDecimalString("1000000", Currency(code = "usd", decimals = 2))
        val s = m.toFormattedString()
        assertEquals("1 000 000.0", s) // decimals null -> shows single 0 by default
        val sExact = m.toFormattedString(decimals = 2)
        assertEquals("1 000 000.00", sExact)
    }

    @Test
    fun `format negative factory input as absolute value`() {
        val m = Moneta.fromDecimalString("-12345.60", Currency(code = "usd", decimals = 2))
        assertEquals("12 345.6", m.toFormattedString())
        assertEquals("12 345.60", m.toFormattedString(decimals = 2))
    }

    @Test
    fun `trim insignificant crypto digits when decimals are null`() {
        val m = Moneta.fromDecimalString("0.00010000", Currency(code = "btc", decimals = 8))
        val s = m.toFormattedString() // decimals null -> trim trailing zeros
        assertEquals("0.0001", s)
    }

    @Test
    fun `show exact crypto decimals when decimals are specified`() {
        val m = Moneta.fromDecimalString("0.00010000", Currency(code = "btc", decimals = 8))
        val s = m.toFormattedString(decimals = 8)
        assertEquals("0.00010000", s)
    }

    @Test
    fun `format zero with null decimals`() {
        val z = Moneta.zero()
        // using usd (2 decimals) and decimals==null -> will create "0.00" internally then trim -> show single zero
        assertEquals("0.0", z.toFormattedString(decimals = null, showDecimalIfZero = true))
        assertEquals("0", z.toFormattedString(decimals = null, showDecimalIfZero = false))
    }

    @Test
    fun `format JPY with explicit zero decimals`() {
        val m = Moneta.fromInt(1000, Currency(code = "jpy", decimals = 0))
        // JPY decimals = 0; explicit decimals=0 -> should not show decimal separator
        assertEquals("1 000", m.toFormattedString(decimals = 0))
    }


    @Test
    fun `throw when requested decimals are out of bounds`() {
        val m = Moneta.fromDecimalString("1.23", Currency(code = "usd", decimals = 2))
        // decimals > currency.decimals should throw
        assertFailsWith<IllegalArgumentException> {
            m.toFormattedString(decimals = 3)
        }
        // negative decimals invalid (should also throw)
        assertFailsWith<IllegalArgumentException> {
            m.toFormattedString(decimals = -1)
        }
    }

    @Test
    fun `preserve fractional leading zeros`() {
        val m = Moneta.fromDecimalString("0.00001230", Currency(code = "btc", decimals = 8))
        // decimals null -> trim trailing zeros -> "0.0000123"
        assertEquals("0.0000123", m.toFormattedString())
        // decimals = 8 -> preserve full 8 digits
        assertEquals("0.00001230", m.toFormattedString(decimals = 8))
    }

    @Test
    fun `format absolute value with custom separators after negative input`() {
        val m = Moneta.fromDecimalString("-1000000.00", Currency(code = "usd", decimals = 2))
        val s = m.toFormattedString(decimals = null, groupSeparator = ',', decimalSeparator = '.')
        assertEquals("1,000,000.0", s)
    }

    @Test
    fun `show padding zeros for exact zero when decimals are specified`() {
        val m = Moneta.fromDecimalString("0", Currency(code = "usd", decimals = 2))
        // decimals specified -> always include decimals digits (0 -> "0.00")
        assertEquals("0.00", m.toFormattedString(decimals = 2))
    }

    @Test
    fun `group very large whole parts`() {
        val m = Moneta.fromDecimalString("123456789012345", Currency(code = "usd", decimals = 2))
        val s = m.toFormattedString(decimals = null)
        // grouping every 3 digits from right
        assertTrue(s.startsWith("123 456 789 012 345"))
    }
}
