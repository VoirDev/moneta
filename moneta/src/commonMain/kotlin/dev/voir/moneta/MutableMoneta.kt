package dev.voir.moneta

/**
 * Editable money state.
 *
 * Keeps the exact text the user typed, including incomplete decimal input like:
 * - "12."
 * - ",5"
 * - "0,"
 *
 * It can expose:
 * - normalized decimal input for editing
 * - parsed Moneta when the input is complete and valid
 * - atomic value using current currency precision
 * - visually formatted input text
 *
 * Currency changes do not destroy the raw input. The same raw input is reinterpreted
 * using the new currency precision.
 */
data class MutableMoneta(
    val rawInput: String,
    val currency: Currency,
) {

    /**
     * Editing-friendly normalized decimal input.
     *
     * Examples:
     * - "1 234,56" -> "1234.56"
     * - "12." -> "12."
     * - ",5" -> "0.5"
     * - "abc" -> null
     */
    val decimalInput: String?
        get() = rawInput.toDecimalInputOrNull()

    /**
     * Final parseable decimal string.
     *
     * Returns null for incomplete inputs such as "12.".
     */
    val decimalString: String?
        get() = decimalInput?.takeUnless { it.endsWith('.') }

    /**
     * Whether the user currently has a syntactically incomplete decimal value.
     *
     * Examples:
     * - "12." => true
     * - "0."  => true
     * - "12.3" => false
     */
    val isPartialDecimal: Boolean
        get() = decimalInput?.endsWith('.') == true

    /**
     * True when the input is blank or contains no meaningful numeric content.
     */
    val isEmpty: Boolean
        get() = rawInput.isBlank() || decimalInput == null

    /**
     * Parsed Moneta when input is complete and valid for the current currency precision.
     *
     * If Moneta rejects the value, returns null.
     */
    val monetaOrNull: Moneta?
        get() = decimalString?.let { decimal ->
            runCatching {
                Moneta.fromDecimalString(
                    decimal,
                    currency = currency,
                )
            }.getOrNull()
        }

    /**
     * Parsed atomic value when input is complete and valid.
     */
    val atomicValueOrNull: Long?
        get() = monetaOrNull?.toAtomicLongOrNull()

    /**
     * Atomic value suitable for forms:
     * - 0 when input is empty
     * - null when input is partial/invalid
     * - parsed value when complete
     */
    val committedAtomicValueOrNull: Long?
        get() = when {
            isEmpty -> 0L
            else -> atomicValueOrNull
        }

    /**
     * Visual formatting for the field.
     *
     * Groups integer digits with spaces and preserves fractional digits exactly as typed.
     * Does not append currency symbol.
     */
    val formattedInput: String
        get() = rawInput.formatDecimalInput()

    /**
     * Replace the raw user input.
     */
    fun onInput(input: String): MutableMoneta =
        copy(rawInput = input)

    /**
     * Rebind this editable money to a new currency.
     *
     * Keeps the raw input unchanged and reinterprets it using the new precision.
     */
    fun withCurrency(currency: Currency?): MutableMoneta =
        copy(
            currency = currency ?: Currency()
        )

    companion object {
        fun empty(currency: Currency?): MutableMoneta =
            MutableMoneta(
                rawInput = "",
                currency = currency ?: Currency()
            )

        fun fromAtomicValue(
            value: Long,
            currency: Currency?,
        ): MutableMoneta {
            val raw = if (value == 0L) {
                ""
            } else {
                runCatching {
                    Moneta.fromAtomicLong(
                        value,
                        currency = currency ?: Currency()
                    ).toDecimalString()
                }.getOrElse { "" }
            }

            return MutableMoneta(
                rawInput = raw,
                currency = currency ?: Currency()
            )
        }
    }
}

/**
 * Editing-friendly normalization.
 *
 * Keeps incomplete decimal input and normalizes separator to '.'.
 */
fun String.toDecimalInputOrNull(): String? {
    val cleaned = trim()
        .replace("\\s+".toRegex(), "")
        .replace("[^\\d.,-]".toRegex(), "")

    if (cleaned.isBlank()) return null

    val isNegative = cleaned.startsWith("-")
    val unsigned = cleaned.removePrefix("-").replace("-", "")

    if (unsigned.isBlank()) return null

    val lastSeparatorIndex = maxOf(
        unsigned.lastIndexOf('.'),
        unsigned.lastIndexOf(','),
    )

    val hasDigits = unsigned.any(Char::isDigit)
    val hasSeparator = lastSeparatorIndex >= 0

    if (!hasDigits && !hasSeparator) return null

    return buildString {
        if (isNegative) append('-')

        if (!hasSeparator) {
            append(unsigned.filter(Char::isDigit))
            return@buildString
        }

        val integerPart = unsigned
            .substring(0, lastSeparatorIndex)
            .filter(Char::isDigit)

        val fractionalPart = unsigned
            .substring(lastSeparatorIndex + 1)
            .filter(Char::isDigit)

        append(integerPart.ifEmpty { "0" })
        append('.')
        append(fractionalPart)
    }.takeIf { it.any(Char::isDigit) }
}

/**
 * Visual-only formatter for decimal input.
 *
 * Examples:
 * - "1234" -> "1 234"
 * - "1234.5" -> "1 234.5"
 * - "12." -> "12."
 * - ",5" -> "0.5"
 */
fun String.formatDecimalInput(): String {
    val normalized = toDecimalInputOrNull() ?: return this

    val negative = normalized.startsWith("-")
    val unsigned = normalized.removePrefix("-")
    val separatorIndex = unsigned.indexOf('.')

    val integerPart = if (separatorIndex >= 0) {
        unsigned.substring(0, separatorIndex)
    } else {
        unsigned
    }

    val fractionalPart = if (separatorIndex >= 0) {
        unsigned.substring(separatorIndex + 1)
    } else {
        ""
    }

    val groupedInteger = integerPart
        .filter(Char::isDigit)
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()

    return buildString {
        if (negative) append('-')
        append(groupedInteger.ifEmpty { "0" })

        if (separatorIndex >= 0) {
            append('.')
            append(fractionalPart)
        }
    }
}
