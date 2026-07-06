package com.budgetpilot.core.ai.data.prompt

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import org.junit.jupiter.api.Test

class PromptRepositoryTest {
    @Test
    fun `loads extraction prompt content from the asset file`() {
        val repository = AssetPromptRepository(ClasspathPromptFileSource())

        val prompt = repository.getPrompt(PromptId.EXTRACTION_V1)

        assertThat(prompt).contains("Receipt extraction v1")
        assertThat(prompt).contains("receipt_type")
    }

    @Test
    fun `loads repair prompt content from the asset file`() {
        val repository = AssetPromptRepository(ClasspathPromptFileSource())

        val prompt = repository.getPrompt(PromptId.REPAIR_V1)

        assertThat(prompt).contains("Repair v1")
        assertThat(prompt).contains("{{malformed_output}}")
    }

    @Test
    fun `loads agent prompt content from the asset file`() {
        val repository = AssetPromptRepository(ClasspathPromptFileSource())

        val prompt = repository.getPrompt(PromptId.AGENT_V1)

        assertThat(prompt).contains("Agent Q&A v1")
        assertThat(prompt).contains("resolve_date_range")
    }

    @Test
    fun `caches prompt content so the file source is only read once per id`() {
        var readCount = 0
        val fileSource =
            PromptFileSource { fileName ->
                readCount++
                "content for $fileName"
            }
        val repository = AssetPromptRepository(fileSource)

        val first = repository.getPrompt(PromptId.EXTRACTION_V1)
        val second = repository.getPrompt(PromptId.EXTRACTION_V1)

        assertThat(first).isEqualTo(second)
        assertThat(readCount).isEqualTo(1)
    }

    @Test
    fun `caches each prompt id independently`() {
        val repository = AssetPromptRepository(ClasspathPromptFileSource())

        val extraction = repository.getPrompt(PromptId.EXTRACTION_V1)
        val repair = repository.getPrompt(PromptId.REPAIR_V1)

        assertThat(extraction).isNotEmpty()
        assertThat(repair).isNotEmpty()
    }
}
