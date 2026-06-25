package ni.edu.uam.raccooncash.util

private val partialMoneyRegex = Regex("^\\d{0,9}([.,]\\d{0,2})?$")
private val completeMoneyRegex = Regex("^\\d+([.,]\\d{1,2})?$")

fun isPotentialMoneyInput(value: String): Boolean {
    return value.isEmpty() || partialMoneyRegex.matches(value)
}

fun parseMoneyInput(value: String): Double? {
    val trimmed = value.trim()
    if (!completeMoneyRegex.matches(trimmed)) {
        return null
    }
    return trimmed.replace(',', '.').toDoubleOrNull()
}

fun formatEditableMoney(value: Double?): String {
    if (value == null) return ""
    return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
}
