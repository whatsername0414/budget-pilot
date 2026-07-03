plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    add("implementation", catalog.findLibrary("kotlinx-serialization-json").get())
}
