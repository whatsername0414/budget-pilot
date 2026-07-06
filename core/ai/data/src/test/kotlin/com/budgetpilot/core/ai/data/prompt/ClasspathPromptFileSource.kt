package com.budgetpilot.core.ai.data.prompt

/**
 * Reads the same files under `src/main/assets/prompts` that [AndroidAssetPromptFileSource]
 * reads on-device, but via the JVM classpath (see the `test` sourceSet's `resources.srcDirs`
 * in this module's build.gradle.kts) — lets [PromptRepository] tests exercise real prompt
 * content without Robolectric.
 */
class ClasspathPromptFileSource : PromptFileSource {
    override fun read(fileName: String): String {
        val stream =
            checkNotNull(javaClass.classLoader?.getResourceAsStream("$PROMPTS_RESOURCE_DIR/$fileName")) {
                "Prompt asset not found on test classpath: $fileName"
            }
        return stream.bufferedReader().use { it.readText() }
    }

    private companion object {
        const val PROMPTS_RESOURCE_DIR = "prompts"
    }
}
