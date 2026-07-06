plugins {
    alias(libs.plugins.budgetpilot.jvm.library)
}

dependencies {
    implementation(project(":core:domain"))
}
