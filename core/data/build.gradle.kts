plugins {
    alias(libs.plugins.budgetpilot.android.library)
    alias(libs.plugins.budgetpilot.koin)
}

android {
    namespace = "com.budgetpilot.core.data"
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
}
