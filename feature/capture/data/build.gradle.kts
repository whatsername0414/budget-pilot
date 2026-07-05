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
    implementation(project(":feature:capture:domain"))

    implementation(libs.kotlinx.coroutines.core)
}
