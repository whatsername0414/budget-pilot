plugins {
    alias(libs.plugins.budgetpilot.android.feature)
    alias(libs.plugins.budgetpilot.serialization)
}

android {
    namespace = "com.budgetpilot.feature.capture.presentation"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:presentation"))
    implementation(project(":core:design-system"))
    implementation(project(":feature:capture:domain"))
    implementation(project(":feature:capture:data"))

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.coil.compose)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.assertk)
}
