plugins {
    alias(libs.plugins.budgetpilot.android.feature)
    alias(libs.plugins.budgetpilot.serialization)
}

android {
    namespace = "com.budgetpilot.feature.ask.presentation"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))
    // Ask has no domain/data submodule of its own (PLAN.md §3/§6 Phase 4) — the agent engine
    // and tools already live under :core:ai because both Ask and Insights share them, so this
    // ViewModel wires AgentSessionFactory/AgentLoop directly rather than through a translation
    // layer like capture's AiError -> ExtractionError mapping (CLAUDE.md §10 2026-07-06 entries).
    implementation(project(":core:ai:domain"))
    implementation(project(":core:ai:data"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.navigation.compose)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.assertk)
}
