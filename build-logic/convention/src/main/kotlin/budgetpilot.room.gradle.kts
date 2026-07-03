plugins {
    id("com.google.devtools.ksp")
    id("androidx.room")
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("implementation", catalog.findLibrary("androidx-room-runtime").get())
    add("implementation", catalog.findLibrary("androidx-room-ktx").get())
    add("ksp", catalog.findLibrary("androidx-room-compiler").get())
}
