package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MonetaFormattingTest {

    private val usd = Currency("USD", 2)
    private val btc = Currency("BTC", 8)
    private val jpy = Currency("JPY", 0) // no decimals

    @Test
    fun group_defaultSeparators_and_trimming_when_decimals_null() {
        val m = Moneta.fromDecimalString("1234.5000", usd)
        // decimals = null -> significant digits up to currency.decimals => trims trailing zeros
        val s = m.toGroupedString(usd)
        assertEquals("1 234.5", s)
    }

    @Test
    fun hide_decimal_when_zero_and_showDecimalIfZero_false() {
        val m = Moneta.fromDecimalString("1234.00", usd)
        val s = m.toGroupedString(usd, decimals = null, showDecimalIfZero = false)
        assertEquals("1 234", s)
    }

    @Test
    fun show_single_zero_when_zero_and_showDecimalIfZero_true() {
        val m = Moneta.fromDecimalString("1234.00", usd)
        val s = m.toGroupedString(usd, decimals = null, showDecimalIfZero = true)
        // trims to no fraction then shows single '0'
        assertEquals("1 234.0", s)
    }

    @Test
    fun exact_decimals_padding_when_decimals_specified() {
        val m = Moneta.fromDecimalString("1234", usd)
        val s = m.toGroupedString(usd, decimals = 2) // always show 2 decimals
        assertEquals("1 234.00", s)
    }

    @Test
    fun custom_group_and_decimal_separator() {
        val m = Moneta.fromDecimalString("1234.56", usd)
        val s = m.toGroupedString(usd, decimals = 2, groupSeparator = '.', decimalSeparator = ',')
        assertEquals("1.234,56", s)
    }

    @Test
    fun big_number_grouping() {
        val m = Moneta.fromDecimalString("1000000", usd)
        val s = m.toGroupedString(usd)
        assertEquals("1 000 000.0", s) // decimals null -> shows single 0 by default
        val sExact = m.toGroupedString(usd, decimals = 2)
        assertEquals("1 000 000.00", sExact)
    }

    @Test
    fun negative_values_formatting() {
        val m = Moneta.fromDecimalString("-12345.60", usd)
        assertEquals("-12 345.6", m.toGroupedString(usd))
        assertEquals("-12 345.60", m.toGroupedString(usd, decimals = 2))
    }

    @Test
    fun crypto_trim_significant_digits_when_null() {
        val m = Moneta.fromDecimalString("0.00010000", btc)
        val s = m.toGroupedString(btc) // decimals null -> trim trailing zeros
        assertEquals("0.0001", s)
    }

    @Test
    fun crypto_show_exact_when_decimals_specified() {
        val m = Moneta.fromDecimalString("0.00010000", btc)
        val s = m.toGroupedString(btc, decimals = 8)
        assertEquals("0.00010000", s)
    }

    @Test
    fun zero_value_behaviour_with_null_decimals() {
        val z = Moneta.zero()
        // using usd (2 decimals) and decimals==null -> will create "0.00" internally then trim -> show single zero
        assertEquals("0.0", z.toGroupedString(usd, decimals = null, showDecimalIfZero = true))
        assertEquals("0", z.toGroupedString(usd, decimals = null, showDecimalIfZero = false))
    }

    @Test
    fun jpy_zero_and_decimals_zero_explicit() {
        val m = Moneta.fromInt(1000, jpy)
        // JPY decimals = 0; explicit decimals=0 -> should not show decimal separator
        assertEquals("1 000", m.toGroupedString(jpy, decimals = 0))
    }


    @Test
    fun decimals_bounds_validation_throws() {
        val m = Moneta.fromDecimalString("1.23", usd)
        // decimals > currency.decimals should throw
        assertFailsWith<IllegalArgumentException> {
            m.toGroupedString(usd, decimals = 3)
        }
        // negative decimals invalid (should also throw)
        assertFailsWith<IllegalArgumentException> {
            m.toGroupedString(usd, decimals = -1)
        }
    }

    @Test
    fun fractional_leading_zeros_are_preserved() {
        val m = Moneta.fromDecimalString("0.00001230", btc)
        // decimals null -> trim trailing zeros -> "0.0000123"
        assertEquals("0.0000123", m.toGroupedString(btc))
        // decimals = 8 -> preserve full 8 digits
        assertEquals("0.00001230", m.toGroupedString(btc, decimals = 8))
    }

    @Test
    fun combined_custom_separators_and_negative_zero() {
        val m = Moneta.fromDecimalString("-1000000.00", usd)
        val s =
            m.toGroupedString(usd, decimals = null, groupSeparator = ',', decimalSeparator = '.')
        assertEquals("-1,000,000.0", s)
    }

    @Test
    fun exact_zero_with_decimals_specified_shows_padding_zeros() {
        val m = Moneta.fromDecimalString("0", usd)
        // decimals specified -> always include decimals digits (0 -> "0.00")
        assertEquals("0.00", m.toGroupedString(usd, decimals = 2))
    }

    @Test
    fun very_large_whole_part_grouping_works() {
        val m = Moneta.fromDecimalString("123456789012345", usd)
        val s = m.toGroupedString(usd, decimals = null)
        // grouping every 3 digits from right
        assertTrue(s.startsWith("123 456 789 012 345"))
    }
}
