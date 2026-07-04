plugins {
    alias(libs.plugins.budgetpilot.android.library)
    alias(libs.plugins.budgetpilot.compose)
}

android {
    namespace = "com.budgetpilot.core.designsystem"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:presentation"))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
}
