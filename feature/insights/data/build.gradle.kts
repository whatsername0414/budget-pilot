plugins {
    alias(libs.plugins.budgetpilot.android.library)
    alias(libs.plugins.budgetpilot.koin)
}

android {
    namespace = "com.budgetpilot.feature.insights.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:ai:domain"))
    implementation(project(":core:ai:data"))
    implementation(project(":feature:insights:domain"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.androidx.datastore.preferences)
}
