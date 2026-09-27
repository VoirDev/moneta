package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MonetaAtomicTest {
    @Test
    fun `atomic string counts smallest units`() {
        assertEquals("124", money("1.235").toAtomicString())
        assertEquals("150", money("1.5").toAtomicString())
        assertEquals("0", Moneta.zero(USD).toAtomicString())
        assertEquals("1000", Moneta.fromInt(1000, JPY).toAtomicString())
    }

    @Test
    fun `atomic string round-trips values beyond Long`() {
        val wei = "123456789012345678901234567890"

        assertEquals(wei, Moneta.fromAtomicString(wei, ETH).toAtomicString())
    }

    @Test
    fun `atomic long round-trips Long bounds`() {
        assertEquals(Long.MAX_VALUE, Moneta.fromAtomicLong(Long.MAX_VALUE, USD).toAtomicLong())
        assertEquals(0L, Moneta.fromAtomicLong(0L, BTC).toAtomicLong())
    }

    @Test
    fun `atomic long overflow returns null or throws`() {
        val tooLarge = Moneta.fromAtomicString("9223372036854775808", JPY)

        assertNull(tooLarge.toAtomicLongOrNull())
        val error = assertFailsWith<ArithmeticException> { tooLarge.toAtomicLong() }
        assertEquals(
            "Atomic amount of Moneta(9223372036854775808 JPY) does not fit in Long: 9223372036854775808.",
            error.message,
        )
    }

    @Test
    fun `whole units convert to atomic units`() {
        assertEquals(100_000_000L, Moneta.fromInt(1, BTC).toAtomicLong())
    }
}
