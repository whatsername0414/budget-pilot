import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

fun Project.configureLintAndFormatting() {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    pluginManager.apply("io.gitlab.arturbosch.detekt")

    extensions.configure<KtlintExtension> {
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
    }

    dependencies.add("ktlintRuleset", catalog.findLibrary("compose-rules-ktlint").get())
    dependencies.add("detektPlugins", catalog.findLibrary("detekt-formatting").get())
    dependencies.add("detektPlugins", catalog.findLibrary("compose-rules-detekt").get())

    extensions.configure<DetektExtension> {
        toolVersion = catalog.findVersion("detektPlugin").get().requiredVersion
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
    }
}
