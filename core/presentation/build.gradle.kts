plugins {
    alias(libs.plugins.budgetpilot.android.library)
    alias(libs.plugins.budgetpilot.compose)
}

android {
    namespace = "com.budgetpilot.core.presentation"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
