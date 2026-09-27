package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MonetaEqualityTest {
    @Test
    fun `equal amounts in the same currency are equal`() {
        assertEquals(money("1.5"), money("1.50"))
        assertEquals(money("1.5").hashCode(), money("1.50").hashCode())
    }

    @Test
    fun `equal amounts in different currencies are not equal`() {
        assertNotEquals(money("1"), money("1", EUR))
    }

    @Test
    fun `amounts work as set and map keys`() {
        val set = setOf(money("1"), money("1.00"), Moneta.fromAtomicInt(100, USD))
        assertEquals(1, set.size)
    }

    @Test
    fun `amounts compare numerically`() {
        assertTrue(money("1.01") > money("1"))
        assertTrue(money("0.99") < money("1"))
        assertEquals(0, money("1").compareTo(money("1.00")))
        assertEquals(money("2"), maxOf(money("1"), money("2")))
        assertEquals(listOf(money("1"), money("2"), money("3")), listOf(money("3"), money("1"), money("2")).sorted())
    }

    @Test
    fun `comparison rejects a different currency`() {
        assertFailsWith<IllegalArgumentException> { money("1") < money("1", EUR) }
    }

    @Test
    fun `isZero reports zero amounts`() {
        assertTrue(Moneta.zero(USD).isZero())
        assertTrue(money("0.00").isZero())
        assertTrue(money("0.001").isZero())
        assertFalse(money("0.01").isZero())
    }

    @Test
    fun `toString shows amount at currency scale and code`() {
        assertEquals("Moneta(1.50 USD)", money("1.5").toString())
        assertEquals("Moneta(0.00010000 BTC)", money("0.0001", BTC).toString())
        assertEquals("Moneta(1.50)", Moneta.fromDecimalString("1.5").toString())
    }
}
