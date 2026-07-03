plugins {
    alias(libs.plugins.budgetpilot.android.application)
    alias(libs.plugins.budgetpilot.compose)
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
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
