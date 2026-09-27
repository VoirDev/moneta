package dev.voir.moneta

import dev.voir.decimal.Decimal
import dev.voir.decimal.Rounding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExchangeRateTest {
    private val myr = Currency(decimals = 2, code = "MYR")

    @Test
    fun `convertByRate multiplies and rounds to target scale`() {
        val converted = money("1").convertByRate(Decimal.parse("4.6"), myr)

        assertEquals(money("4.60", myr), converted)
    }

    @Test
    fun `convertByRate rounds a high precision product once`() {
        val converted = money("1.23456789", BTC).convertByRate(Decimal.parse("45000.12345678"), USD)

        // 1.23456789 * 45000.12345678 = 55555.707466...; HALF_UP to cents.
        assertEquals(money("55555.71"), converted)
    }

    @Test
    fun `convertByRate honours explicit rounding`() {
        val converted = money("1").convertByRate(Decimal.parse("0.125"), EUR, Rounding.HALF_EVEN)

        assertEquals(money("0.12", EUR), converted)
    }

    @Test
    fun `convertByRate rejects zero and negative rates`() {
        assertFailsWith<IllegalArgumentException> { money("1").convertByRate(Decimal.zero(), myr) }
        val error = assertFailsWith<IllegalArgumentException> {
            money("1").convertByRate(Decimal.parse("-2"), myr)
        }
        assertEquals("Exchange rate must be positive, but was -2.", error.message)
    }

    @Test
    fun `calculateExchangeRate divides target by source`() {
        val rate = calculateExchangeRate(money("2.00"), money("9.20", myr))

        assertEquals(Decimal.parse("4.6"), rate)
    }

    @Test
    fun `calculateExchangeRate respects scale and rounding`() {
        assertEquals(Decimal.parse("0.3333"), calculateExchangeRate(money("3"), money("1", EUR), scale = 4))
        assertEquals(
            Decimal.parse("0.6666"),
            calculateExchangeRate(money("3"), money("2", EUR), scale = 4, rounding = Rounding.DOWN),
        )
    }

    @Test
    fun `calculateExchangeRate rejects zero amounts`() {
        val error = assertFailsWith<IllegalArgumentException> {
            calculateExchangeRate(money("0.00"), money("1", myr))
        }
        assertEquals("Cannot calculate an exchange rate: from amount Moneta(0.00 USD) is zero.", error.message)
        assertFailsWith<IllegalArgumentException> { calculateExchangeRate(money("1"), Moneta.zero(myr)) }
    }

    @Test
    fun `calculateReverseExchangeRate inverts the rate`() {
        assertEquals(Decimal.parse("0.25"), calculateReverseExchangeRate(Decimal.parse("4")))
        assertEquals(Decimal.parse("0.2174"), calculateReverseExchangeRate(Decimal.parse("4.6"), scale = 4))
    }

    @Test
    fun `calculateReverseExchangeRate rejects zero and negative rates`() {
        assertFailsWith<IllegalArgumentException> { calculateReverseExchangeRate(Decimal.zero()) }
        assertFailsWith<IllegalArgumentException> { calculateReverseExchangeRate(Decimal.parse("-1")) }
    }

    @Test
    fun `calculateExchangeRatesPair returns direct and reverse rates`() {
        val (direct, reverse) = calculateExchangeRatesPair(money("2.00"), money("9.20", myr), scale = 6)

        assertEquals(Decimal.parse("4.6"), direct)
        assertEquals(Decimal.parse("0.217391"), reverse)
    }

    @Test
    fun `calculateExchangeRatesPair rounds reverse rate from amounts`() {
        // From the rounded direct rate 66.67 the reverse would be 0.01; from the amounts it is 0.015 -> 0.02.
        val (direct, reverse) = calculateExchangeRatesPair(money("3"), money("200", EUR), scale = 2)

        assertEquals(Decimal.parse("66.67"), direct)
        assertEquals(Decimal.parse("0.02"), reverse)
    }

    @Test
    fun `calculateExchangeRatesPair keeps reverse rate when direct rate rounds to zero`() {
        val (direct, reverse) = calculateExchangeRatesPair(
            from = Moneta.fromInt(1_000_000_000, USD),
            to = Moneta.fromAtomicInt(1, BTC),
            scale = 12,
        )

        assertEquals(Decimal.zero(), direct)
        assertEquals(Decimal.parse("100000000000000000"), reverse)
    }

    @Test
    fun `calculateExchangeRatesPair rejects zero amounts`() {
        assertFailsWith<IllegalArgumentException> { calculateExchangeRatesPair(Moneta.zero(USD), money("1", myr)) }
        assertFailsWith<IllegalArgumentException> { calculateExchangeRatesPair(money("1"), Moneta.zero(myr)) }
    }
}
