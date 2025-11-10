package dev.voir.moneta

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExchangeRateTest {
    @Test
    fun convertByRate_simpleUsdToMyr() {
        val oneUsd = Moneta.fromInt(1, code = "usd", decimals = 2) // 1.00 USD
        val rate = Decimal.of("4.6") // 4.6 MYR per 1 USD

        val converted = oneUsd.convertByRate(rate = rate, targetCode = "myr", targetDecimals = 2)
        // 1.00 * 4.6 = 4.6 -> rounded to 2 decimals => 4.60
        assertEquals("4.60", converted.toDecimalString(2))
    }

    @Test
    fun convertByRate_cryptoPrecision() {
        // Convert BTC -> USD using a high-precision rate
        val satoshiAtomic = Moneta.fromAtomicLong(
            123456789L, code = "btc", decimals = 8
        ) // 1.23456789 BTC
        val usdPerBtc = Decimal.of("45000.12345678")              // USD per BTC (high precision)

        val converted = satoshiAtomic.convertByRate(
            usdPerBtc, targetCode = "usd", targetDecimals = 2
        )
        // Multiply then round to 2 decimals
        val expectedRaw = Decimal.of("1.23456789").multiply(usdPerBtc)
        val expectedRounded = expectedRaw.setScale(2, Rounding.HALF_UP)
        assertEquals(expectedRounded.toPlainString(), converted.toDecimalString(2))
    }

    @Test
    fun calculateExchangeRate_and_reverse_pair() {
        val from = Moneta.fromDecimalString("1.00", code = "usd", decimals = 2)
        val to = Moneta.fromDecimalString("4.60", code = "myr", decimals = 2)

        val direct = calculateExchangeRate(from, to, scale = 8)
        val reverse = calculateReverseExchangeRate(direct, scale = 18)

        // direct should be 4.60 (with requested scale)
        assertEquals("4.6", direct.setScale(2, Rounding.HALF_UP).toPlainString())
        // reverse should be approx 0.21739130 (1 / 4.6) when scale=8 => check by multiplying direct*reverse ≈ 1
        val product = direct.multiply(reverse).setScale(8, Rounding.HALF_UP)
        assertEquals("1", product.toPlainString())
    }

    @Test
    fun calculateExchangeRatesPair_convenience() {
        val from = Moneta.fromDecimalString("2.00", code = "usd", decimals = 2)
        val to = Moneta.fromDecimalString(
            "9.20",
            code = "myr",
            decimals = 2
        ) // implies 4.6 MYR per 1 USD

        val (direct, reverse) = calculateExchangeRatesPair(from, to, scale = 18)
        assertEquals("4.6", direct.setScale(1, Rounding.HALF_UP).toPlainString())
        // reverse * direct ≈ 1
        val prod = direct.multiply(reverse).setScale(6, Rounding.HALF_UP)
        assertEquals("1", prod.toPlainString())
    }

    @Test
    fun errors_when_rate_or_from_is_zero() {
        val zeroUsd = Moneta.zero()
        val someMyr = Moneta.fromDecimalString("1.00", code = "myr", decimals = 2)
        val zeroRate = Decimal.ofInteger("0")

        // calculateExchangeRate should throw when from is zero
        assertFailsWith<ArithmeticException> {
            calculateExchangeRate(zeroUsd, someMyr)
        }

        // calculateReverseExchangeRate should throw on zero rate
        assertFailsWith<ArithmeticException> {
            calculateReverseExchangeRate(zeroRate)
        }

        // convertByRate should throw on zero rate
        val oneUsd = Moneta.fromInt(1, code = "usd", decimals = 2)
        assertFailsWith<ArithmeticException> {
            oneUsd.convertByRate(zeroRate, targetCode = "myr", targetDecimals = 2)
        }
    }
}
