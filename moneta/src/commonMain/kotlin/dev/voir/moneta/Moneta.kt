package dev.voir.moneta

import kotlin.jvm.JvmInline

/**
 * A compact wrapper for money amounts. Internally uses platform Decimal backend.
 * The `value` is always stored as a decimal with arbitrary precision.
 */
@JvmInline
value class Money private constructor(val value: Decimal) {

    companion object {
        fun fromInt(
            value: Int,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money {
            // treat as whole units: move point left by currency.decimals
            val integerStr = value.toString()
            val dec = Decimal.ofInteger(integerStr).movePointLeft(currency.decimals)
            val scaled = dec.setScale(currency.decimals, rounding)
            return Money(scaled)
        }

        fun fromLong(
            value: Long,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money {
            val integerStr = value.toString()
            val dec = Decimal.ofInteger(integerStr).movePointLeft(currency.decimals)
            val scaled = dec.setScale(currency.decimals, rounding)
            return Money(scaled)
        }

        fun fromShort(
            value: Short,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money =
            fromInt(value.toInt(), currency, rounding)

        fun fromByte(
            value: Byte,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money =
            fromInt(value.toInt(), currency, rounding)

        /**
         * From Double/Float — uses Decimal.of(value.toString()). Note: binary floating types may introduce
         * representation artifacts (e.g. 0.1 -> 0.10000000000000001). If you need exact decimals, use
         * `fromDecimalString(...)` or `fromDoubleExact(...)` below.
         */
        fun fromDouble(
            value: Double,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money {
            val d = Decimal.of(value.toString())
            val scaled = d.setScale(currency.decimals, rounding)
            return Money(scaled)
        }

        fun fromFloat(
            value: Float,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money {
            val d = Decimal.of(value.toString())
            val scaled = d.setScale(currency.decimals, rounding)
            return Money(scaled)
        }

        /**
         * If you have a decimal textual representation that is the source of truth, use this to avoid
         * Double/Float representation artifacts:
         *
         * Example: Money.fromDecimalStringExact("0.1", usd) will produce exactly 0.10 USD.
         */
        fun fromDecimalStringExact(
            decimalText: String,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money {
            val d = Decimal.of(decimalText)
            val scaled = d.setScale(currency.decimals, rounding)
            return Money(scaled)
        }

        /**
         * Construct from atomic integer counts (smallest units). These constructors expect the caller is
         * passing atomic units (e.g., cents, satoshis, wei).
         */
        fun fromAtomicInt(
            atomic: Int,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money {
            val d = Decimal.ofInteger(atomic.toString()).movePointLeft(currency.decimals)
            val scaled = d.setScale(currency.decimals, rounding)
            return Money(scaled)
        }

        fun fromAtomicLong(
            atomic: Long,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money {
            val d = Decimal.ofInteger(atomic.toString()).movePointLeft(currency.decimals)
            val scaled = d.setScale(currency.decimals, rounding)
            return Money(scaled)
        }

        fun fromAtomicString(
            atomic: String,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money {
            val d = Decimal.ofInteger(atomic).movePointLeft(currency.decimals)
            val scaled = d.setScale(currency.decimals, rounding)
            return Money(scaled)
        }

        /**
         * Generic Number constructor. Routes to the best available constructor based on runtime type.
         */
        fun fromNumber(
            number: Number,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money {
            return when (number) {
                is Int -> fromInt(number, currency, rounding)
                is Long -> fromLong(number, currency, rounding)
                is Short -> fromShort(number, currency, rounding)
                is Byte -> fromByte(number, currency, rounding)
                is Double -> fromDouble(number, currency, rounding)
                is Float -> fromFloat(number, currency, rounding)
                else -> {
                    // fallback: use toString()
                    val d = Decimal.of(number.toString())
                    val scaled = d.setScale(currency.decimals, rounding)
                    Money(scaled)
                }
            }
        }

        /** Create from decimal string like "123.45" and currency decimals validation/rounding optional. */
        fun fromDecimalString(
            decimal: String,
            currency: Currency,
            rounding: Rounding = Rounding.HALF_UP
        ): Money {
            val d = Decimal.of(decimal)
            // optionally round to currency.decimals when constructing if required
            val scaled = d.setScale(currency.decimals, rounding)
            return Money(scaled)
        }

        /** Create from atomic integer string (smallest units) */
        fun fromAtomicString(atomic: String, currency: Currency): Money {
            // atomic is integer number of smallest units, e.g., cents, satoshis, wei
            val d = Decimal.ofInteger(atomic).movePointLeft(currency.decimals)
            return Money(d)
        }

        /** Create zero */
        fun zero() = Money(Decimal.zero())
    }

    fun plus(other: Money): Money = Money(this.value.add(other.value))

    fun minus(other: Money): Money = Money(this.value.subtract(other.value))

    fun times(factor: Long): Money = Money(this.value.multiply(factor.toString()))

    fun timesDecimal(factor: Decimal): Money = Money(this.value.multiply(factor))

    fun divide(factor: Long, scale: Int = 18, rounding: Rounding = Rounding.HALF_UP): Money =
        Money(this.value.divide(factor.toString(), scale, rounding))

    fun toDecimalString(scale: Int? = null): String =
        if (scale == null) value.toPlainString() else value.setScale(scale, Rounding.HALF_UP)
            .toPlainString()

    /** Return atomic integer string (rounded HALF_UP to currency decimals). */
    fun toAtomicString(currency: Currency, rounding: Rounding = Rounding.HALF_UP): String {
        // move decimal point right by currency.decimals and round to integer
        val shifted = value.setScale(currency.decimals, rounding).movePointRight(currency.decimals)
        return shifted.toIntegerString()
    }

    override fun toString(): String = value.toPlainString()
}
