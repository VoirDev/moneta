# Moneta — immutable money value for Kotlin Multiplatform

`Moneta` is an immutable monetary amount that carries its own `Currency`. It is built on
[`dev.voir:decimal`](https://github.com/VoirDev/decimal-kmp), so amounts are exact decimals with no
floating-point artifacts, for fiat and crypto alike.

Targets: JVM, Android, iOS (arm64, x64, simulator arm64), and macOS arm64.

## Installation

```kotlin
dependencies {
    implementation("dev.voir:moneta:2.0.0")
}
```

`dev.voir:decimal` is exposed as an `api` dependency, so `Decimal` and `Rounding` are available
to your code as well.

## Core rules

Every `Moneta` holds two invariants:

1. **It is never negative.** Moneta models an amount of money, not a signed balance; whether money
   comes in or goes out belongs to your domain model. Factories store the absolute value of their
   input, and operations that would go negative throw `IllegalArgumentException`.
2. **It never has more fractional digits than `Currency.decimals`.** Factories and every
   arithmetic operation round to that scale (`Rounding.HALF_UP` unless you pass another mode), so
   an amount always converts exactly to atomic units such as cents, satoshis, or wei.

Arithmetic and comparison require the **same `Currency`** on both sides; mixing currencies throws
`IllegalArgumentException`. Equality is numeric and includes the currency: `1.5 USD == 1.50 USD`,
but `1 USD != 1 EUR`.

## Currencies

```kotlin
val usd = Currency(decimals = 2, code = "USD", symbol = "$")
val jpy = Currency(decimals = 0, code = "JPY", symbol = "¥")
val btc = Currency(decimals = 8, code = "BTC", symbol = "₿")
val eth = Currency(decimals = 18, code = "ETH")
```

`decimals` must not be negative. `code` and `symbol` are optional metadata; all three properties
take part in currency equality.

## Creating amounts

```kotlin
// Decimal text: preferred for user input, JSON, and CSV
Moneta.fromDecimalString("1.235", usd)          // 1.24 USD
Moneta.fromDecimalString("1.239", usd, Rounding.DOWN) // 1.23 USD
"19.99".toMoneta(usd)                            // 19.99 USD

// Whole units
Moneta.fromInt(10, usd)                          // 10.00 USD
5L.toMoneta(btc)                                 // 5.00000000 BTC

// Atomic units (cents, satoshis, wei)
Moneta.fromAtomicLong(150_000_000L, btc)         // 1.5 BTC
Moneta.fromAtomicString("1000000000000000000", eth) // 1 ETH
150.toAtomicMoneta(usd)                          // 1.50 USD

// Existing Decimal values
Moneta.fromDecimal(Decimal.parse("0.333"), usd)  // 0.33 USD

// Floating point: uses the printed digits, so 0.1 stays 0.1
Moneta.fromDouble(0.1, usd)                      // 0.10 USD

// Zero
Moneta.zero(usd)                                 // 0.00 USD
```

Invalid text throws `IllegalArgumentException`, as do `NaN` and infinite floating-point values.

## Arithmetic

```kotlin
val a = Moneta.fromDecimalString("10.00", usd)
val b = Moneta.fromDecimalString("2.50", usd)

a + b                              // 12.50 USD
a - b                              // 7.50 USD
b - a                              // throws: the result would be negative
b * 3                              // 7.50 USD
a * Decimal.parse("0.075")         // 0.75 USD (rounded to cents)
a / 3                              // 3.33 USD (rounded to cents)
a.divide(Decimal.fromInt(3), Rounding.UP) // 3.34 USD

a > b                              // true
listOf(a, b).sorted()              // [2.50 USD, 10.00 USD]
a.isZero()                         // false
```

### Splitting without losing cents

Rounded division does not add back up: `10.00 / 3 * 3 == 9.99`. Use `split` to share an amount;
the parts always add up exactly, and leftover atomic units go to the first parts.

```kotlin
Moneta.fromInt(10, usd).split(3)   // [3.34, 3.33, 3.33] USD
```

## Changing value or currency

```kotlin
val amount = Moneta.zero(usd)
    .withDecimalString("2.40")     // 2.40 USD
    .withCurrency(btc)             // 2.40000000 BTC (re-labels, no exchange rate)

amount.withValue(Decimal.parse("9.99"))
amount.withAtomicLong(1234L)
```

`withCurrency` only re-labels an amount; use exchange-rate conversion to convert it.

## Exchange rates

Rates are expressed as target units per one source unit and must be positive.

```kotlin
val myr = Currency(decimals = 2, code = "MYR")

Moneta.fromInt(1, usd).convertByRate(Decimal.parse("4.6"), myr)  // 4.60 MYR

val from = Moneta.fromDecimalString("2.00", usd)
val to = Moneta.fromDecimalString("9.20", myr)

calculateExchangeRate(from, to)                   // 4.6 (MYR per USD)
calculateReverseExchangeRate(Decimal.parse("4.6"), scale = 4) // 0.2174
val (direct, reverse) = calculateExchangeRatesPair(from, to, scale = 6) // 4.6, 0.217391
```

`calculateExchangeRatesPair` computes both rates from the amounts, so the reverse rate never
inherits rounding from the direct one. Choose a `scale` large enough for your currencies; very
small rates round to zero when the scale is too low.

## Output

```kotlin
val m = Moneta.fromDecimalString("1234567.5", usd)

m.toDecimalString()                // "1234567.5"
m.toDecimalString(scale = 2)       // "1234567.50"
m.toFormattedString()              // "1 234 567.5"
m.toFormattedString(decimals = 2, groupSeparator = '.', decimalSeparator = ',') // "1.234.567,50"
m.toFormattedString(appendSymbol = true) // "1 234 567.5$"
m.toAtomicString()                 // "123456750"
m.toAtomicLong()                   // 123456750, throws ArithmeticException on Long overflow
m.toAtomicLongOrNull()             // 123456750, or null on Long overflow
m.toString()                       // "Moneta(1234567.50 USD)" — for logs, not for UI
```

Persist amounts with `toAtomicString()` (or `toAtomicLong()`) together with the currency, and
restore them with `fromAtomicString(...)`; the round trip is exact.

## License

This project is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).
