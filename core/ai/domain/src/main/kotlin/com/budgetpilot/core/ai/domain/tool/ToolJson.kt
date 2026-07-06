package com.budgetpilot.core.ai.domain.tool

import com.budgetpilot.core.domain.money.Money
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.abs

internal val MONTH_PATTERN = Regex("""\d{4}-\d{2}""")

internal fun JsonObject.stringOrNull(key: String): String? {
    val element = this[key] ?: return null
    if (element is JsonNull) return null
    val primitive = element as? JsonPrimitive ?: return null
    return primitive.content.takeIf { it.isNotBlank() }
}

internal fun Money.toPesoString(): String {
    val sign = if (centavos < 0) "-" else ""
    val absCentavos = abs(centavos)
    val whole = absCentavos / 100
    val fraction = absCentavos % 100
    return "$sign$whole.${fraction.toString().padStart(2, '0')}"
}
