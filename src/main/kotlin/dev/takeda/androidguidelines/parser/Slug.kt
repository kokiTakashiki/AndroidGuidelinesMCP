package dev.takeda.androidguidelines.parser

internal object Slug {
    fun of(text: String): String {
        val normalized =
            text
                .lowercase()
                .replace('_', '-')
                .replace('/', '-')
                .replace(Regex("[^a-z0-9\\- ]"), "")
                .trim()
                .replace(Regex("\\s+"), "-")
                .replace(Regex("-+"), "-")
        return normalized.trim('-')
    }
}
