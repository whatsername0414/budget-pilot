package com.budgetpilot.core.domain.merchant

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test

class PhMerchantCatalogTest {
    @Test
    fun `matches an exact known merchant name`() {
        assertThat(PhMerchantCatalog.suggestCategory("Jollibee")).isEqualTo("Food")
    }

    @Test
    fun `matches a known merchant embedded in longer receipt text`() {
        assertThat(
            PhMerchantCatalog.suggestCategory("SM SUPERMARKET STA. ROSA #04521"),
        ).isEqualTo("Shopping")
    }

    @Test
    fun `matches are case-insensitive`() {
        assertThat(PhMerchantCatalog.suggestCategory("mercury drug")).isEqualTo("Health")
        assertThat(PhMerchantCatalog.suggestCategory("MERCURY DRUG")).isEqualTo("Health")
    }

    @Test
    fun `matches gcash and maya as bills`() {
        assertThat(PhMerchantCatalog.suggestCategory("GCash")).isEqualTo("Bills")
        assertThat(PhMerchantCatalog.suggestCategory("Maya")).isEqualTo("Bills")
    }

    @Test
    fun `matches a hyphenated merchant name regardless of spacing`() {
        assertThat(PhMerchantCatalog.suggestCategory("7-Eleven Ortigas")).isEqualTo("Groceries")
        assertThat(PhMerchantCatalog.suggestCategory("7 Eleven Ortigas")).isEqualTo("Groceries")
    }

    @Test
    fun `returns null for an unknown merchant`() {
        assertThat(PhMerchantCatalog.suggestCategory("Random Sari-Sari Store")).isNull()
    }

    @Test
    fun `returns null for blank input`() {
        assertThat(PhMerchantCatalog.suggestCategory("   ")).isNull()
    }

    @Test
    fun `does not false-match a short merchant key inside an unrelated word`() {
        assertThat(PhMerchantCatalog.suggestCategory("Cosmetics World")).isNull()
    }
}
