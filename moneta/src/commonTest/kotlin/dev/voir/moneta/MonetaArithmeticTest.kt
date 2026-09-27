package dev.voir.moneta

import dev.voir.decimal.Decimal
import dev.voir.decimal.Rounding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MonetaArithmeticTest {
    @Test
    fun `plus adds amounts exactly`() {
        assertEquals(money("12.75"), money("10.50") + money("2.25"))
    }

    @Test
    fun `plus is associative and commutative`() {
        val x = money("1.10")
        val y = money("2.20")
        val z = money("3.30")

        assertEquals((x + y) + z, x + (y + z))
        assertEquals(x + y, y + x)
    }

    @Test
    fun `plus rejects a different currency`() {
        val error = assertFailsWith<IllegalArgumentException> { money("1") + money("1", EUR) }
        assertEquals(
            "Cannot add amounts in different currencies: " +
                "Currency(decimals=2, code=USD, symbol=$) and Currency(decimals=2, code=EUR, symbol=€).",
            error.message,
        )
    }

    @Test
    fun `plus treats a different symbol as a different currency`() {
        assertFailsWith<IllegalArgumentException> { money("1") + money("1", USD.copy(symbol = "US$")) }
    }

    @Test
    fun `minus subtracts amounts exactly`() {
        assertEquals(money("3.75"), money("5.00") - money("1.25"))
    }

    @Test
    fun `minus down to zero is allowed`() {
        assertEquals(Moneta.zero(USD), money("5") - money("5"))
    }

    @Test
    fun `minus rejects a negative result`() {
        val error = assertFailsWith<IllegalArgumentException> { money("1") - money("2") }
        assertEquals(
            "Cannot subtract Moneta(2.00 USD) from Moneta(1.00 USD): the result would be negative.",
            error.message,
        )
    }

    @Test
    fun `minus rejects a different currency`() {
        assertFailsWith<IllegalArgumentException> { money("2") - money("1", EUR) }
    }

    @Test
    fun `times multiplies by whole quantities`() {
        val price = money("2.50")

        assertEquals(money("7.50"), price * 3)
        assertEquals(money("7.50"), price * 3L)
        assertEquals(Moneta.zero(USD), price * 0)
        assertEquals(money("100.00"), money("0.01") * 10_000)
        assertEquals(money("100.000001", BTC), money("1.00000001", BTC) * 100)
    }

    @Test
    fun `times rejects negative quantities`() {
        assertFailsWith<IllegalArgumentException> { money("1") * -2 }
        assertFailsWith<IllegalArgumentException> { money("1") * -2L }
    }

    @Test
    fun `times by decimal rounds to currency scale`() {
        assertEquals(money("0.33"), money("1") * Decimal.parse("0.3333"))
        assertEquals(money("2.47"), money("19.99") * Decimal.parse("0.1235"))
    }

    @Test
    fun `multiply honours explicit rounding`() {
        assertEquals(money("2.46"), money("19.99").multiply(Decimal.parse("0.1235"), Rounding.DOWN))
    }

    @Test
    fun `multiply rejects a negative factor`() {
        assertFailsWith<IllegalArgumentException> { money("1") * Decimal.parse("-0.5") }
    }

    @Test
    fun `div rounds once to currency scale`() {
        assertEquals(money("3.33"), money("10") / 3)
        assertEquals(money("3.33"), money("10") / 3L)
        assertEquals(money("2.50"), money("10") / 4)
        assertEquals(money("0.67"), money("2") / 3)
        assertEquals(money("4"), money("10") / Decimal.parse("2.5"))
    }

    @Test
    fun `divide honours explicit rounding`() {
        assertEquals(money("3.34"), money("10").divide(Decimal.fromInt(3), Rounding.UP))
        assertEquals(money("0.66"), money("2").divide(Decimal.fromInt(3), Rounding.DOWN))
    }

    @Test
    fun `div rejects a negative divisor`() {
        assertFailsWith<IllegalArgumentException> { money("1") / -2 }
    }

    @Test
    fun `div by zero fails`() {
        assertFailsWith<ArithmeticException> { money("1") / 0 }
        assertFailsWith<ArithmeticException> { money("1") / Decimal.zero() }
    }

    @Test
    fun `results never exceed currency decimals`() {
        val third = money("10") / 3

        assertEquals("3.33", third.toDecimalString())
        assertEquals(money("9.99"), third * 3)
    }

    @Test
    fun `jpy arithmetic stays in whole units`() {
        assertEquals(Moneta.fromInt(333, JPY), Moneta.fromInt(1000, JPY) / 3)
    }
}
