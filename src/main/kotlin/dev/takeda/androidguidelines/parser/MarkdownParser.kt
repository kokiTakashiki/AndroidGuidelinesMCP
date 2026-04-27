package dev.takeda.androidguidelines.parser

import dev.takeda.androidguidelines.model.Section
import dev.takeda.androidguidelines.model.SourceId

class MarkdownParser {
    private val headingRegex = Regex("^(#{1,6})\\s+(.+?)\\s*$")
    private val inlineDecoration = Regex("\\s*\\{[^}]*}\\s*$")

    fun parse(
        sourceId: SourceId,
        markdown: String,
    ): List<Section> {
        val lines = markdown.lines()
        val sections = mutableListOf<Section>()
        val pathStack = ArrayDeque<Pair<Int, String>>()
        var currentLevel = -1
        var currentTitle: String? = null
        var currentAnchor: String? = null
        val buffer = StringBuilder()
        var inFence = false
        var fenceMarker: String? = null

        fun flush() {
            val title = currentTitle ?: return
            sections +=
                Section(
                    sourceId = sourceId,
                    sectionPath = pathStack.map { it.second }.toList(),
                    anchor = currentAnchor ?: Slug.of(title),
                    title = title,
                    body = buffer.toString().trim(),
                )
            buffer.setLength(0)
        }

        for (raw in lines) {
            val line = raw

            if (inFence) {
                buffer.appendLine(line)
                if (line.trimStart().startsWith(fenceMarker!!)) {
                    inFence = false
                    fenceMarker = null
                }
                continue
            }
            val trimmed = line.trimStart()
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                inFence = true
                fenceMarker = if (trimmed.startsWith("```")) "```" else "~~~"
                buffer.appendLine(line)
                continue
            }

            val heading = headingRegex.matchEntire(line)
            if (heading != null) {
                flush()
                val level = heading.groupValues[1].length
                val rawTitle = heading.groupValues[2].replace(inlineDecoration, "").trim()
                while (pathStack.isNotEmpty() && pathStack.last().first >= level) {
                    pathStack.removeLast()
                }
                pathStack.addLast(level to rawTitle)
                currentLevel = level
                currentTitle = rawTitle
                currentAnchor = Slug.of(rawTitle)
            } else {
                if (currentTitle != null) {
                    buffer.appendLine(line)
                }
            }
        }
        flush()
        return sections.filter { it.body.isNotBlank() || it.sectionPath.size <= 2 }
    }
}
