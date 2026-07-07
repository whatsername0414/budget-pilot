package com.budgetpilot.core.domain.preferences

/**
 * Pure combination rule shared by the extraction router and the Q&A agent's LLM client: private
 * mode always wins, regardless of the independent cloud-AI toggle's own stored value (DESIGN-SPEC.md
 * §12 — the Cloud AI row is dimmed with its switch forced off while private mode is on, but the
 * underlying preference itself is left untouched so it "remembers" its prior value once private
 * mode is turned back off).
 */
object CloudAiAvailability {
    fun isAllowed(
        cloudAiEnabled: Boolean,
        privateModeEnabled: Boolean,
    ): Boolean = cloudAiEnabled && !privateModeEnabled
}
