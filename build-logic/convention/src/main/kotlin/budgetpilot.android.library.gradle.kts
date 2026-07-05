plugins {
    id("com.android.library")
}

android {
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    lint {
        lintConfig = rootProject.file("config/lint/lint.xml")
    }
}

configureLintAndFormatting()

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    add("testImplementation", platform(catalog.findLibrary("junit5-bom").get()))
    add("testImplementation", catalog.findLibrary("junit5-jupiter").get())
    add("testRuntimeOnly", catalog.findLibrary("junit5-jupiter-engine").get())
    add("testRuntimeOnly", catalog.findLibrary("junit5-platform-launcher").get())
    add("testImplementation", catalog.findLibrary("assertk").get())
    add("testImplementation", catalog.findLibrary("turbine").get())
    add("testImplementation", catalog.findLibrary("kotlinx-coroutines-test").get())
}
