plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    add("testImplementation", platform(catalog.findLibrary("junit5-bom").get()))
    add("testImplementation", catalog.findLibrary("junit5-jupiter").get())
    add("testRuntimeOnly", catalog.findLibrary("junit5-jupiter-engine").get())
    add("testImplementation", catalog.findLibrary("assertk").get())
    add("testImplementation", catalog.findLibrary("turbine").get())
    add("testImplementation", catalog.findLibrary("kotlinx-coroutines-test").get())
}
