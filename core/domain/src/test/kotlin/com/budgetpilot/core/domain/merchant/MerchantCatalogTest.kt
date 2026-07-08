package com.budgetpilot.core.domain.merchant

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test

class MerchantCatalogTest {
    @Test
    fun `matches an exact known merchant name`() {
        assertThat(MerchantCatalog.suggestCategory("Jollibee")).isEqualTo("Food")
    }

    @Test
    fun `matches a known merchant embedded in longer receipt text`() {
        assertThat(
            MerchantCatalog.suggestCategory("SM SUPERMARKET STA. ROSA #04521"),
        ).isEqualTo("Shopping")
    }

    @Test
    fun `matches are case-insensitive`() {
        assertThat(MerchantCatalog.suggestCategory("mercury drug")).isEqualTo("Health")
        assertThat(MerchantCatalog.suggestCategory("MERCURY DRUG")).isEqualTo("Health")
    }

    @Test
    fun `matches gcash and maya as bills`() {
        assertThat(MerchantCatalog.suggestCategory("GCash")).isEqualTo("Bills")
        assertThat(MerchantCatalog.suggestCategory("Maya")).isEqualTo("Bills")
    }

    @Test
    fun `matches a hyphenated merchant name regardless of spacing`() {
        assertThat(MerchantCatalog.suggestCategory("7-Eleven Ortigas")).isEqualTo("Groceries")
        assertThat(MerchantCatalog.suggestCategory("7 Eleven Ortigas")).isEqualTo("Groceries")
    }

    @Test
    fun `returns null for an unknown merchant`() {
        assertThat(MerchantCatalog.suggestCategory("Random Sari-Sari Store")).isNull()
    }

    @Test
    fun `returns null for blank input`() {
        assertThat(MerchantCatalog.suggestCategory("   ")).isNull()
    }

    @Test
    fun `does not false-match a short merchant key inside an unrelated word`() {
        assertThat(MerchantCatalog.suggestCategory("Cosmetics World")).isNull()
    }
}
