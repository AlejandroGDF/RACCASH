package ni.edu.uam.raccooncash.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

private const val maxMoneyIntegerDigits = 12
private val completeMoneyRegex = Regex("^(\\d+|\\d{1,3}(,\\d{3})+)(\\.\\d{1,2})?$")
private val moneySymbols = DecimalFormatSymbols(Locale.US).apply {
    groupingSeparator = ','
    decimalSeparator = '.'
}

fun isPotentialMoneyInput(value: String): Boolean {
    if (value.isEmpty()) return true
    if (value.any { !it.isDigit() && it != ',' && it != '.' }) return false
    if (value.count { it == '.' } > 1) return false

    val decimalIndex = value.indexOf('.')
    val integerPart = if (decimalIndex == -1) value else value.substring(0, decimalIndex)
    val decimalPart = if (decimalIndex == -1) "" else value.substring(decimalIndex + 1)

    if (decimalPart.contains(',') || decimalPart.length > 2) return false
    if (integerPart.count { it.isDigit() } > maxMoneyIntegerDigits) return false
    if (integerPart.startsWith(',') || integerPart.contains(",,")) return false

    if (integerPart.contains(',')) {
        val groups = integerPart.split(',')
        if (groups.first().length !in 1..3) return false
        if (groups.drop(1).any { it.length > 3 }) return false
    }

    return true
}

fun parseMoneyInput(value: String): Double? {
    val trimmed = value.trim()
    if (!completeMoneyRegex.matches(trimmed)) {
        return null
    }
    return trimmed.replace(",", "").toDoubleOrNull()
}

fun formatMoneyAmount(value: Double, precision: Int = 2): String {
    val safePrecision = precision.coerceAtLeast(0)
    return moneyFormatter(
        minimumFractionDigits = safePrecision,
        maximumFractionDigits = safePrecision
    ).format(value)
}

fun formatCurrencyAmount(value: Double, currency: String = "C$", precision: Int = 2): String {
    val sign = if (value < 0.0) "-" else ""
    return "$sign$currency${formatMoneyAmount(abs(value), precision)}"
}

fun formatEditableMoney(value: Double?): String {
    if (value == null) return ""
    return moneyFormatter(
        minimumFractionDigits = 0,
        maximumFractionDigits = 2
    ).format(value)
}

private fun moneyFormatter(minimumFractionDigits: Int, maximumFractionDigits: Int): DecimalFormat {
    return DecimalFormat().apply {
        decimalFormatSymbols = moneySymbols
        isGroupingUsed = true
        this.minimumFractionDigits = minimumFractionDigits
        this.maximumFractionDigits = maximumFractionDigits
    }
}
