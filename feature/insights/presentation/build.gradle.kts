plugins {
    alias(libs.plugins.budgetpilot.android.feature)
}

android {
    namespace = "com.budgetpilot.feature.insights.presentation"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))
    implementation(project(":feature:insights:domain"))
    implementation(project(":feature:insights:data"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)

    // Only needed to construct InsightCheckUseCase's InsightMessageComposer collaborator in
    // tests (fake LlmClient/PromptRepository) — not a main dependency of this module.
    testImplementation(project(":core:ai:domain"))
    testImplementation(project(":core:ai:data"))
}
