dependencies {
    add("implementation", platform(catalog.findLibrary("koin-bom").get()))
    add("implementation", catalog.findLibrary("koin-core").get())
    add("implementation", catalog.findLibrary("koin-android").get())
    add("implementation", catalog.findLibrary("koin-androidx-compose").get())
}
