import java.util.Properties

plugins {
    alias(libs.plugins.budgetpilot.android.library)
    alias(libs.plugins.budgetpilot.serialization)
}

android {
    namespace = "com.budgetpilot.core.ai.data"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val localProperties =
            Properties().apply {
                val localPropertiesFile = rootProject.file("local.properties")
                if (localPropertiesFile.exists()) {
                    localPropertiesFile.inputStream().use(::load)
                }
            }
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"",
        )
    }

    // Local unit tests run on the plain JVM with no Robolectric/AssetManager, so the
    // prompt files aren't readable via Context.assets there — exposing the same
    // src/main/assets files as test resources lets tests load them via the JVM
    // classpath instead, with no duplicated copies.
    sourceSets {
        getByName("test") {
            resources.srcDirs("src/main/assets")
        }
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ai:domain"))
    implementation(project(":core:data"))

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.ktor.client.mock)
}
