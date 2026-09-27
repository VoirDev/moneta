package dev.voir.moneta

import dev.voir.decimal.Decimal
import dev.voir.decimal.Rounding
import kotlin.test.Test
import kotlin.test.assertEquals

class ToMonetaExtensionsTest {
    @Test
    fun `integer extensions create whole units`() {
        assertEquals(money("5"), 5.toMoneta(USD))
        assertEquals(money("123"), 123L.toMoneta(USD))
        assertEquals(money("2"), 2.toShort().toMoneta(USD))
        assertEquals(money("1"), 1.toByte().toMoneta(USD))
    }

    @Test
    fun `floating extensions round to currency scale`() {
        assertEquals(money("1.24"), 1.235.toMoneta(USD))
        assertEquals(money("1.23"), 1.239.toMoneta(USD, Rounding.DOWN))
        assertEquals(money("0.125", BTC), 0.125f.toMoneta(BTC))
    }

    @Test
    fun `number extension dispatches by runtime type`() {
        val whole: Number = 7
        val fractional: Number = 0.125

        assertEquals(money("7"), whole.toMoneta(USD))
        assertEquals(money("0.125", BTC), fractional.toMoneta(BTC))
    }

    @Test
    fun `decimal and string extensions parse whole units`() {
        assertEquals(money("1.24"), Decimal.parse("1.235").toMoneta(USD))
        assertEquals(money("1.23"), "1.235".toMoneta(USD, Rounding.DOWN))
    }

    @Test
    fun `atomic extensions count smallest units`() {
        assertEquals(money("1.50"), 150.toAtomicMoneta(USD))
        assertEquals(money("1", BTC), 100_000_000L.toAtomicMoneta(BTC))
        assertEquals(money("0.000000000000000001", ETH), "1".toAtomicMoneta(ETH))
    }

    @Test
    fun `extensions store absolute value`() {
        assertEquals(money("3"), (-3).toMoneta(USD))
        assertEquals(money("0.50"), (-0.5).toMoneta(USD))
    }
}
