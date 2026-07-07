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
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:ai:data"))
    implementation(project(":feature:history:presentation"))
    implementation(project(":feature:budgets:presentation"))
    implementation(project(":feature:home:presentation"))
    implementation(project(":feature:capture:presentation"))
    implementation(project(":feature:capture:data"))
    implementation(project(":feature:settings:presentation"))
    implementation(project(":feature:ask:presentation"))
    implementation(project(":feature:insights:data"))
    implementation(project(":feature:insights:presentation"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.koin.androidx.workmanager)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
