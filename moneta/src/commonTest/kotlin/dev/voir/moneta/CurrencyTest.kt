package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CurrencyTest {
    @Test
    fun `default currency has two decimals and no metadata`() {
        assertEquals(Currency(decimals = 2, code = null, symbol = null), Currency())
    }

    @Test
    fun `zero decimals are allowed`() {
        assertEquals(0, Currency(decimals = 0).decimals)
    }

    @Test
    fun `negative decimals are rejected`() {
        val error = assertFailsWith<IllegalArgumentException> { Currency(decimals = -1) }
        assertEquals("Currency decimals must not be negative, but was -1.", error.message)
    }
}
