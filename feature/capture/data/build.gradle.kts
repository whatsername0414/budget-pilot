plugins {
    alias(libs.plugins.budgetpilot.android.library)
    alias(libs.plugins.budgetpilot.koin)
}

android {
    namespace = "com.budgetpilot.feature.capture.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:ai:domain"))
    implementation(project(":core:ai:data"))
    implementation(project(":feature:capture:domain"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.mlkit.text.recognition)
}
