package dev.voir.moneta

import kotlin.test.*

class MutableMonetaTest {

    private val defaultCurrency = Currency()
    private val usd = Currency(code = "USD", decimals = 2, symbol = "$")
    private val btc = Currency(code = "BTC", decimals = 8, symbol = "₿")

    @Test
    fun `decimalInput returns null for blank input`() {
        assertNull(MutableMoneta("", usd).decimalInput)
        assertNull(MutableMoneta("   ", usd).decimalInput)
    }

    @Test
    fun `decimalInput normalizes grouped comma decimal`() {
        val value = MutableMoneta("1 234,56", usd)
        assertEquals("1234.56", value.decimalInput)
    }

    @Test
    fun `decimalInput preserves partial decimal`() {
        assertEquals("12.", MutableMoneta("12.", usd).decimalInput)
        assertEquals("0.5", MutableMoneta(",5", usd).decimalInput)
        assertEquals("0.", MutableMoneta("0,", usd).decimalInput)
    }

    @Test
    fun `decimalInput returns null for non numeric input`() {
        assertNull(MutableMoneta("abc", usd).decimalInput)
        assertNull(MutableMoneta("--", usd).decimalInput)
    }

    @Test
    fun `decimalString returns null for partial decimal`() {
        assertNull(MutableMoneta("12.", usd).decimalString)
        assertNull(MutableMoneta("0,", usd).decimalString)
    }

    @Test
    fun `decimalString returns normalized value for complete decimal`() {
        assertEquals("12.3", MutableMoneta("12,3", usd).decimalString)
        assertEquals("1234.56", MutableMoneta("1 234,56", usd).decimalString)
        assertEquals("1234", MutableMoneta("1 234", usd).decimalString)
    }

    @Test
    fun `isPartialDecimal is true only for unfinished decimal`() {
        assertTrue(MutableMoneta("12.", usd).isPartialDecimal)
        assertTrue(MutableMoneta("0,", usd).isPartialDecimal)

        assertFalse(MutableMoneta("12.3", usd).isPartialDecimal)
        assertFalse(MutableMoneta("12", usd).isPartialDecimal)
        assertFalse(MutableMoneta("abc", usd).isPartialDecimal)
    }

    @Test
    fun `isEmpty is true for blank or meaningless input`() {
        assertTrue(MutableMoneta("", usd).isEmpty)
        assertTrue(MutableMoneta("   ", usd).isEmpty)
        assertTrue(MutableMoneta("abc", usd).isEmpty)
        assertTrue(MutableMoneta("--", usd).isEmpty)
    }

    @Test
    fun `isEmpty is false for partial numeric input`() {
        assertFalse(MutableMoneta("12.", usd).isEmpty)
        assertFalse(MutableMoneta(",5", usd).isEmpty)
        assertFalse(MutableMoneta("0", usd).isEmpty)
    }

    @Test
    fun `monetaOrNull returns null for partial decimal`() {
        assertNull(MutableMoneta("12.", usd).monetaOrNull)
        assertNull(MutableMoneta(",", usd).monetaOrNull)
    }

    @Test
    fun `monetaOrNull parses complete value`() {
        val moneta = MutableMoneta("12.34", usd).monetaOrNull

        assertNotNull(moneta)
        assertEquals(usd, moneta?.currency)
        assertEquals("12.34", moneta?.toDecimalString())
    }

    @Test
    fun `monetaOrNull uses current currency precision`() {
        val usdValue = MutableMoneta("1.23", usd).monetaOrNull
        val btcValue = MutableMoneta("1.23", btc).monetaOrNull

        assertNotNull(usdValue)
        assertNotNull(btcValue)
        assertEquals("1.23", usdValue.toDecimalString())
        assertEquals("1.23000000", btcValue.toDecimalString(scale = 8))
    }

    @Test
    fun `atomicValueOrNull returns null for partial or invalid input`() {
        assertNull(MutableMoneta("12.", usd).atomicValueOrNull)
        assertNull(MutableMoneta("abc", usd).atomicValueOrNull)
    }

    @Test
    fun `atomicValueOrNull returns parsed atomic value for valid input`() {
        assertEquals(1234L, MutableMoneta("12.34", usd).atomicValueOrNull)
        assertEquals(50L, MutableMoneta(".50", usd).atomicValueOrNull)
        assertEquals(123000000L, MutableMoneta("1.23", btc).atomicValueOrNull)
    }

    @Test
    fun `committedAtomicValueOrNull returns zero for empty input`() {
        assertEquals(0L, MutableMoneta("", usd).committedAtomicValueOrNull)
        assertEquals(0L, MutableMoneta("   ", usd).committedAtomicValueOrNull)
        assertEquals(0L, MutableMoneta("abc", usd).committedAtomicValueOrNull)
    }

    @Test
    fun `committedAtomicValueOrNull returns null for partial decimal`() {
        assertNull(MutableMoneta("12.", usd).committedAtomicValueOrNull)
        assertNull(MutableMoneta("0,", usd).committedAtomicValueOrNull)
    }

    @Test
    fun `committedAtomicValueOrNull returns atomic value for complete valid input`() {
        assertEquals(1234L, MutableMoneta("12.34", usd).committedAtomicValueOrNull)
    }

    @Test
    fun `formattedInput groups integer part and preserves fraction`() {
        assertEquals("1 234", MutableMoneta("1234", usd).formattedInput)
        assertEquals("1 234.5", MutableMoneta("1234.5", usd).formattedInput)
        assertEquals("12.", MutableMoneta("12.", usd).formattedInput)
        assertEquals("0.5", MutableMoneta(",5", usd).formattedInput)
    }

    @Test
    fun `formattedInput returns raw text for non numeric input`() {
        assertEquals("abc", MutableMoneta("abc", usd).formattedInput)
        assertEquals("--", MutableMoneta("--", usd).formattedInput)
    }

    @Test
    fun `onInput replaces raw input`() {
        val initial = MutableMoneta("12", usd)
        val updated = initial.onInput("45.67")

        assertEquals("12", initial.rawInput)
        assertEquals("45.67", updated.rawInput)
        assertEquals(usd, updated.currency)
    }

    @Test
    fun `withCurrency keeps raw input and changes currency`() {
        val initial = MutableMoneta("12.34", usd)
        val updated = initial.withCurrency(btc)

        assertEquals("12.34", updated.rawInput)
        assertEquals(btc, updated.currency)
    }

    @Test
    fun `withCurrency falls back to default currency when null`() {
        val updated = MutableMoneta("12.34", usd).withCurrency(null)
        assertEquals(defaultCurrency, updated.currency)
    }

    @Test
    fun `withCurrency reinterprets value with new precision`() {
        val usdValue = MutableMoneta("1.23", usd)
        val btcValue = usdValue.withCurrency(btc)

        assertEquals(123L, usdValue.atomicValueOrNull)
        assertEquals(123000000L, btcValue.atomicValueOrNull)
    }

    @Test
    fun `empty creates blank state with provided currency`() {
        val value = MutableMoneta.empty(usd)

        assertEquals("", value.rawInput)
        assertEquals(usd, value.currency)
        assertTrue(value.isEmpty)
        assertEquals(0L, value.committedAtomicValueOrNull)
    }

    @Test
    fun `empty falls back to default currency when null`() {
        val value = MutableMoneta.empty(null)

        assertEquals("", value.rawInput)
        assertEquals(defaultCurrency, value.currency)
    }

    @Test
    fun `fromAtomicValue creates blank input for zero`() {
        val value = MutableMoneta.fromAtomicValue(0L, usd)

        assertEquals("", value.rawInput)
        assertEquals(usd, value.currency)
        assertTrue(value.isEmpty)
    }

    @Test
    fun `fromAtomicValue creates decimal input from atomic value`() {
        val value = MutableMoneta.fromAtomicValue(1234L, usd)

        assertEquals("12.34", value.rawInput)
        assertEquals(usd, value.currency)
        assertEquals(1234L, value.atomicValueOrNull)
    }

    @Test
    fun `fromAtomicValue uses provided currency precision`() {
        val value = MutableMoneta.fromAtomicValue(123000000L, btc)

        assertEquals("1.23", value.rawInput)
        assertEquals(btc, value.currency)
        assertEquals(123000000L, value.atomicValueOrNull)
    }

    @Test
    fun `fromAtomicValue falls back to default currency when null`() {
        val value = MutableMoneta.fromAtomicValue(1234L, null)

        assertEquals(defaultCurrency, value.currency)
    }

    @Test
    fun `toDecimalInputOrNull handles integers`() {
        assertEquals("123", "123".toDecimalInputOrNull())
        assertEquals("1234", "1 234".toDecimalInputOrNull())
    }

    @Test
    fun `toDecimalInputOrNull handles decimal separators`() {
        assertEquals("12.34", "12.34".toDecimalInputOrNull())
        assertEquals("12.34", "12,34".toDecimalInputOrNull())
        assertEquals("1234.56", "1 234,56".toDecimalInputOrNull())
        assertEquals("1234.56", "1,234.56".toDecimalInputOrNull())
    }

    @Test
    fun `toDecimalInputOrNull keeps partial decimals`() {
        assertEquals("12.", "12.".toDecimalInputOrNull())
        assertEquals("12.", "12,".toDecimalInputOrNull())
        assertEquals("0.5", ",5".toDecimalInputOrNull())
        assertEquals("0.5", ".5".toDecimalInputOrNull())
        assertEquals("0.", ",".replace(",", "0,").toDecimalInputOrNull()) // sanity path
    }

    @Test
    fun `toDecimalInputOrNull strips unrelated symbols`() {
        assertEquals("1234.56", "$1 234,56".toDecimalInputOrNull())
        assertEquals("99.95", "EUR 99,95".toDecimalInputOrNull())
    }

    @Test
    fun `toDecimalInputOrNull handles negative input`() {
        assertEquals("-123", "-123".toDecimalInputOrNull())
        assertEquals("-12.34", "-12,34".toDecimalInputOrNull())
        assertEquals("-0.5", "-,5".toDecimalInputOrNull())
    }

    @Test
    fun `toDecimalInputOrNull returns null for meaningless input`() {
        assertNull("".toDecimalInputOrNull())
        assertNull("   ".toDecimalInputOrNull())
        assertNull("abc".toDecimalInputOrNull())
        assertNull("--".toDecimalInputOrNull())
    }

    @Test
    fun `formatDecimalInput formats integers`() {
        assertEquals("1 234", "1234".formatDecimalInput())
        assertEquals("1 234 567", "1234567".formatDecimalInput())
    }

    @Test
    fun `formatDecimalInput formats decimals`() {
        assertEquals("1 234.56", "1234.56".formatDecimalInput())
        assertEquals("1 234.56", "1 234,56".formatDecimalInput())
        assertEquals("12.", "12.".formatDecimalInput())
        assertEquals("0.5", ",5".formatDecimalInput())
    }

    @Test
    fun `formatDecimalInput preserves negative sign`() {
        assertEquals("-1 234", "-1234".formatDecimalInput())
        assertEquals("-1 234.56", "-1234,56".formatDecimalInput())
    }

    @Test
    fun `formatDecimalInput returns raw input when it cannot normalize`() {
        assertEquals("abc", "abc".formatDecimalInput())
        assertEquals("--", "--".formatDecimalInput())
        assertEquals("", "".formatDecimalInput())
    }
}
