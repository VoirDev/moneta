# Moneta — Immutable money value object (Kotlin Multiplatform)

`Moneta` is a compact, immutable value object representing a monetary amount **together with its
currency metadata**.

---

## Features

- Stores **amount + currency code + decimals** in one object
- Decimal-based using high-precision `Decimal` (no floating artifacts)
- Supports fiat & crypto (e.g., USD 2 decimals, BTC 8 decimals, ERC‑20 tokens)
- Exact atomic conversions (cents, satoshis, wei)

---

## Why Moneta?

When money is represented only as a decimal value (`BigDecimal`, `Decimal`, etc.), the caller must
_also track_ currency code & decimals. This leads to risk:

```kotlin
val amount = BigDecimal("1.23") // but what currency? what scale?
```

With **Moneta**, a monetary value **carries its own currency metadata**:

```kotlin
val m = Moneta.fromDecimalString("1.235", code = "USD", decimals = 2)
// m = 1.24 USD (HALF_UP rounding default)
```

No need to pass around a separate `Currency` object.

---

## Quick Start

### Installation

Add the following dependency to your project:

```gradle
dependencies {
    implementation("dev.voir.moneta:1.0.0")
}
```

### Constructing values

```kotlin
// Whole-unit constructors
val usd = Moneta.fromInt(10, code = "USD", decimals = 2)    // 10.00 USD
val btc = Moneta.fromLong(1, code = "BTC", decimals = 8)    // 1.00000000 BTC

// Precise decimal input (avoid Float/Double artifacts)
val exact = Moneta.fromDecimalString("0.1", code = "USD", decimals = 2) // 0.10 USD

// From atomic smallest units (cents, satoshis, wei)
val sats = Moneta.fromAtomicLong(150000000, code = "BTC", decimals = 8)
```

### Arithmetic

All operations preserve currency metadata.

```kotlin
val a = Moneta.fromDecimalString("1.50", code = "USD", decimals = 2)
val b = Moneta.fromDecimalString("2.00", code = "USD", decimals = 2)

val c = a.plus(b)   // -> 3.50 USD
val d = b.minus(a)  // -> 0.50 USD
val e = a.times(3)  // -> 4.50 USD
```

### Atomic conversion (for persistence)

```kotlin
val m = Moneta.fromDecimalString("1.235", code = "USD", decimals = 2)
val atomic = m.toAtomicString() // "124"
```

---

## License

This project is licensed under the GNU Lesser General Public License v3.0.

See the full license at https://www.gnu.org/licenses/lgpl-3.0.txt
