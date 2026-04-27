package dev.takeda.androidguidelines.source

import dev.takeda.androidguidelines.model.Section
import dev.takeda.androidguidelines.model.SourceId
import dev.takeda.androidguidelines.parser.HtmlParser
import dev.takeda.androidguidelines.parser.MarkdownParser
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

class SourceLoader(
    private val dataRoot: Path,
) {
    private val md = MarkdownParser()
    private val html = HtmlParser()

    fun loadAll(): List<Section> {
        val all = mutableListOf<Section>()
        all += loadKotlinConventions()
        all += loadKotlinStyleMigration()
        all += loadComposeApi()
        all += loadComposeComponentApi()
        all += loadJavaStyle()
        return all
    }

    private fun loadKotlinConventions(): List<Section> =
        parseMarkdownIfExists(
            dataRoot.resolve("kotlin-web-site/docs/topics/coding-conventions.md"),
            SourceId.KOTLIN_CONVENTIONS,
        )

    private fun loadKotlinStyleMigration(): List<Section> =
        parseMarkdownIfExists(
            dataRoot.resolve("kotlin-web-site/docs/topics/code-style-migration-guide.md"),
            SourceId.KOTLIN_STYLE_MIGRATION,
        )

    private fun loadComposeApi(): List<Section> =
        parseMarkdownIfExists(
            dataRoot.resolve("androidx/compose/docs/compose-api-guidelines.md"),
            SourceId.COMPOSE_API,
        )

    private fun loadComposeComponentApi(): List<Section> =
        parseMarkdownIfExists(
            dataRoot.resolve("androidx/compose/docs/compose-component-api-guidelines.md"),
            SourceId.COMPOSE_COMPONENT_API,
        )

    private fun loadJavaStyle(): List<Section> {
        val file = dataRoot.resolve("styleguide/javaguide.html")
        if (!file.exists() || !file.isRegularFile()) return emptyList()
        return html.parse(SourceId.JAVA_STYLE, file.readText())
    }

    private fun parseMarkdownIfExists(
        path: Path,
        sourceId: SourceId,
    ): List<Section> {
        if (!path.exists() || !path.isRegularFile()) return emptyList()
        return md.parse(sourceId, path.readText())
    }

    companion object {
        fun defaultDataRoot(): Path {
            val env = System.getenv("ANDROID_GUIDELINES_DATA")
            if (!env.isNullOrBlank()) return Path.of(env)
            return Path.of(System.getProperty("user.dir"), ".guidelines-data")
        }
    }
}
