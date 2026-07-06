package com.budgetpilot.core.ai.data.prompt

import android.content.Context

class AndroidAssetPromptFileSource(
    private val context: Context,
) : PromptFileSource {
    override fun read(fileName: String): String =
        context.assets
            .open("$PROMPTS_ASSET_DIR/$fileName")
            .bufferedReader()
            .use { it.readText() }

    private companion object {
        const val PROMPTS_ASSET_DIR = "prompts"
    }
}
