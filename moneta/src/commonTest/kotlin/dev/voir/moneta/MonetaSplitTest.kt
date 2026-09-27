package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MonetaSplitTest {
    @Test
    fun `split distributes leftover units to the first parts`() {
        assertEquals(listOf(money("3.34"), money("3.33"), money("3.33")), money("10").split(3))
    }

    @Test
    fun `split into even parts has no leftover`() {
        assertEquals(List(4) { money("2.50") }, money("10").split(4))
    }

    @Test
    fun `split into one part returns the amount`() {
        assertEquals(listOf(money("1.23")), money("1.23").split(1))
    }

    @Test
    fun `split with more parts than atomic units yields zeros`() {
        assertEquals(
            listOf(money("0.01"), money("0.01"), Moneta.zero(USD), Moneta.zero(USD)),
            money("0.02").split(4),
        )
    }

    @Test
    fun `split of zero yields zeros`() {
        assertEquals(List(3) { Moneta.zero(USD) }, Moneta.zero(USD).split(3))
    }

    @Test
    fun `split works for currencies without decimals`() {
        val parts = Moneta.fromInt(100, JPY).split(3)

        assertEquals(listOf(34, 33, 33).map { Moneta.fromInt(it, JPY) }, parts)
    }

    @Test
    fun `split parts always add up to the original amount`() {
        val amounts = listOf(money("0.01"), money("99.99"), money("1234567.89"), money("0.00000007", BTC))
        for (amount in amounts) {
            for (parts in 1..12) {
                val shares = amount.split(parts)
                assertEquals(parts, shares.size)
                assertEquals(amount, shares.reduce(Moneta::plus), "$amount split into $parts")
            }
        }
    }

    @Test
    fun `split preserves large atomic amounts`() {
        val amount = Moneta.fromAtomicString("1000000000000000000000000000001", ETH)
        val shares = amount.split(2)

        assertEquals(Moneta.fromAtomicString("500000000000000000000000000001", ETH), shares[0])
        assertEquals(Moneta.fromAtomicString("500000000000000000000000000000", ETH), shares[1])
    }

    @Test
    fun `split rejects non-positive parts`() {
        assertFailsWith<IllegalArgumentException> { money("1").split(0) }
        assertFailsWith<IllegalArgumentException> { money("1").split(-1) }
    }
}
