plugins {
    alias(libs.plugins.budgetpilot.android.library)
    alias(libs.plugins.budgetpilot.room)
    alias(libs.plugins.budgetpilot.koin)
}

android {
    namespace = "com.budgetpilot.core.database"
}

dependencies {
    implementation(project(":core:domain"))

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.assertk)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
