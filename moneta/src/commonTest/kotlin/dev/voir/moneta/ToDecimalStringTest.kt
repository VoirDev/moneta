package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals

class ToDecimalStringTest {

    @Test
    fun `returns null for blank input`() {
        assertEquals(null, "".toDecimalStringOrNull())
        assertEquals(null, "   ".toDecimalStringOrNull())
    }

    @Test
    fun `returns null when input has no digits`() {
        assertEquals(null, "abc".toDecimalStringOrNull())
        assertEquals(null, "--".toDecimalStringOrNull())
        assertEquals(null, ", . -".toDecimalStringOrNull())
        assertEquals(null, ".".toDecimalStringOrNull())
        assertEquals(null, ",".toDecimalStringOrNull())
        assertEquals(null, "-,".toDecimalStringOrNull())
        assertEquals(null, "-.".toDecimalStringOrNull())
    }

    @Test
    fun `keeps plain integer`() {
        assertEquals("123", "123".toDecimalStringOrNull())
    }

    @Test
    fun `removes spaces from grouped integer`() {
        assertEquals("10000", "10 000".toDecimalStringOrNull())
        assertEquals("1234567", "1 234 567".toDecimalStringOrNull())
    }

    @Test
    fun `supports comma as decimal separator`() {
        assertEquals("10.5", "10,5".toDecimalStringOrNull())
        assertEquals("0.5", ",5".toDecimalStringOrNull())
    }

    @Test
    fun `supports dot as decimal separator`() {
        assertEquals("10.5", "10.5".toDecimalStringOrNull())
        assertEquals("0.5", ".5".toDecimalStringOrNull())
    }

    @Test
    fun `uses last separator as decimal separator`() {
        assertEquals("1234.56", "1.234,56".toDecimalStringOrNull())
        assertEquals("1234.56", "1,234.56".toDecimalStringOrNull())
        assertEquals("1234567.89", "1,234,567.89".toDecimalStringOrNull())
        assertEquals("1234567.89", "1.234.567,89".toDecimalStringOrNull())
    }

    @Test
    fun `removes trailing decimal separator`() {
        assertEquals("12", "12.".toDecimalStringOrNull())
        assertEquals("12", "12,".toDecimalStringOrNull())
    }

    @Test
    fun `keeps only leading minus sign`() {
        assertEquals("-123", "-123".toDecimalStringOrNull())
        assertEquals("-1234.56", "-1,234.56".toDecimalStringOrNull())
        assertEquals("-123", "-1-2-3".toDecimalStringOrNull())
    }

    @Test
    fun `removes unrelated symbols`() {
        assertEquals("1234.56", "$1 234,56".toDecimalStringOrNull())
        assertEquals("99.95", "EUR 99,95".toDecimalStringOrNull())
        assertEquals("-42", "=-42abc".toDecimalStringOrNull())
    }

    @Test
    fun `returns zero-prefixed decimal when integer part is missing`() {
        assertEquals("0.75", ",75".toDecimalStringOrNull())
        assertEquals("0.75", ".75".toDecimalStringOrNull())
    }
}
