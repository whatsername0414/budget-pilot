plugins {
    alias(libs.plugins.budgetpilot.android.application)
    alias(libs.plugins.budgetpilot.compose)
    alias(libs.plugins.budgetpilot.koin)
    alias(libs.plugins.budgetpilot.serialization)
}

android {
    namespace = "com.budgetpilot"

    defaultConfig {
        applicationId = "com.budgetpilot.app"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":core:design-system"))
    implementation(project(":feature:expenses:presentation"))
    implementation(project(":feature:budgets:presentation"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
