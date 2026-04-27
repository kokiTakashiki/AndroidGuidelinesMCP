package dev.takeda.androidguidelines.model

enum class SourceId(
    val slug: String,
    val displayName: String,
) {
    KOTLIN_CONVENTIONS("kotlin-conventions", "Kotlin Coding Conventions"),
    KOTLIN_STYLE_MIGRATION("kotlin-style-migration", "Kotlin Style Migration Guide"),
    COMPOSE_API("compose-api", "Compose API Guidelines"),
    COMPOSE_COMPONENT_API("compose-component-api", "Compose Component API Guidelines"),
    JAVA_STYLE("java-style", "Google Java Style Guide"),
}

data class Section(
    val sourceId: SourceId,
    val sectionPath: List<String>,
    val anchor: String,
    val title: String,
    val body: String,
) {
    val resourceUri: String
        get() = "android-guidelines://${sourceId.slug}/$anchor"
}
