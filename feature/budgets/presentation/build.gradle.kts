plugins {
    alias(libs.plugins.budgetpilot.android.feature)
    alias(libs.plugins.budgetpilot.serialization)
}

android {
    namespace = "com.budgetpilot.feature.budgets.presentation"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.vico.multiplatform)
}
