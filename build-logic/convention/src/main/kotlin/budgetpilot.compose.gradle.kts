import com.android.build.api.dsl.CommonExtension

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<CommonExtension> {
    buildFeatures.compose = true
}

dependencies {
    val bom = platform(catalog.findLibrary("androidx-compose-bom").get())
    add("implementation", bom)
    add("implementation", catalog.findLibrary("androidx-compose-ui").get())
    add("implementation", catalog.findLibrary("androidx-compose-ui-graphics").get())
    add("implementation", catalog.findLibrary("androidx-compose-ui-tooling-preview").get())
    add("debugImplementation", catalog.findLibrary("androidx-compose-ui-tooling").get())
    add("debugImplementation", catalog.findLibrary("androidx-compose-ui-test-manifest").get())
    add("androidTestImplementation", bom)
    add("androidTestImplementation", catalog.findLibrary("androidx-compose-ui-test-junit4").get())
}
