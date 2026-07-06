plugins {
    alias(libs.plugins.budgetpilot.jvm.library)
    alias(libs.plugins.budgetpilot.serialization)
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.kotlinx.coroutines.core)
}
