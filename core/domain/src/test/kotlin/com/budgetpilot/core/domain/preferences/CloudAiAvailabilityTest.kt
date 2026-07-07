package com.budgetpilot.core.domain.preferences

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class CloudAiAvailabilityTest {
    @Test
    fun `allowed when cloud AI is on and private mode is off`() {
        assertThat(CloudAiAvailability.isAllowed(cloudAiEnabled = true, privateModeEnabled = false)).isTrue()
    }

    @Test
    fun `not allowed when cloud AI is off and private mode is off`() {
        assertThat(CloudAiAvailability.isAllowed(cloudAiEnabled = false, privateModeEnabled = false)).isFalse()
    }

    @Test
    fun `not allowed when cloud AI is on but private mode is on`() {
        assertThat(CloudAiAvailability.isAllowed(cloudAiEnabled = true, privateModeEnabled = true)).isFalse()
    }

    @Test
    fun `not allowed when both cloud AI is off and private mode is on`() {
        assertThat(CloudAiAvailability.isAllowed(cloudAiEnabled = false, privateModeEnabled = true)).isFalse()
    }
}
