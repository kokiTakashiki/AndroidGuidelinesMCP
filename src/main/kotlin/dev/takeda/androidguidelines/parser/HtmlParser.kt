package dev.takeda.androidguidelines.parser

import dev.takeda.androidguidelines.model.Section
import dev.takeda.androidguidelines.model.SourceId
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class HtmlParser {
    fun parse(
        sourceId: SourceId,
        html: String,
    ): List<Section> {
        val doc = Jsoup.parse(html)
        val main = doc.selectFirst("main") ?: doc.body()
        val headings = main.select("h2, h3")
        val sections = mutableListOf<Section>()
        val pathStack = ArrayDeque<Pair<Int, String>>()

        for (heading in headings) {
            val level = heading.tagName().removePrefix("h").toIntOrNull() ?: continue
            val title = heading.text().trim()
            if (title.isEmpty()) continue
            val anchor = (heading.id().takeIf { it.isNotBlank() } ?: Slug.of(title))

            while (pathStack.isNotEmpty() && pathStack.last().first >= level) {
                pathStack.removeLast()
            }
            pathStack.addLast(level to title)

            val body = collectUntilNextHeading(heading)
            sections +=
                Section(
                    sourceId = sourceId,
                    sectionPath = pathStack.map { it.second }.toList(),
                    anchor = anchor,
                    title = title,
                    body = body.trim(),
                )
        }
        return sections.filter { it.body.isNotBlank() }
    }

    private fun collectUntilNextHeading(heading: Element): String {
        val sb = StringBuilder()
        var node = heading.nextElementSibling()
        while (node != null && node.tagName() !in stopTags) {
            sb.appendLine(node.text())
            node = node.nextElementSibling()
        }
        return sb.toString()
    }

    private val stopTags = setOf("h1", "h2", "h3")
}
