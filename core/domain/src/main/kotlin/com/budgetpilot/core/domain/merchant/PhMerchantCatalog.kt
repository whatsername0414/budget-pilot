package com.budgetpilot.core.domain.merchant

/**
 * Known Philippine merchants mapped to a suggested expense category.
 * Matching is fuzzy (case-insensitive, word-bounded substring) so it still
 * hits real receipt/OCR text like "SM SUPERMARKET STA ROSA #04521".
 */
object PhMerchantCatalog {

    private val merchantCategories: List<Pair<String, String>> = listOf(
        "jollibee" to "Food",
        "mcdonald" to "Food",
        "kfc" to "Food",
        "chowking" to "Food",
        "mang inasal" to "Food",
        "sm" to "Shopping",
        "robinsons" to "Shopping",
        "puregold" to "Groceries",
        "7-eleven" to "Groceries",
        "7 eleven" to "Groceries",
        "landers" to "Groceries",
        "mercury drug" to "Health",
        "watsons" to "Health",
        "grab" to "Transport",
        "angkas" to "Transport",
        "gcash" to "Bills",
        "maya" to "Bills",
        "meralco" to "Bills",
        "manila water" to "Bills",
        "globe" to "Bills",
        "smart" to "Bills",
        "pldt" to "Bills",
    )

    /** Returns the suggested category for [merchantText], or null when no known merchant matches. */
    fun suggestCategory(merchantText: String): String? {
        val normalized = merchantText.trim().lowercase()
        if (normalized.isEmpty()) return null

        return merchantCategories.firstOrNull { (merchant, _) ->
            matches(normalized, merchant)
        }?.second
    }

    private fun matches(normalized: String, merchant: String): Boolean {
        val pattern = Regex("\\b${Regex.escape(merchant)}\\b")
        return pattern.containsMatchIn(normalized)
    }
}
