package dev.takeda.androidguidelines.server

import dev.takeda.androidguidelines.model.Section
import dev.takeda.androidguidelines.model.SourceId
import dev.takeda.androidguidelines.search.Bm25Index
import dev.takeda.androidguidelines.search.SearchHit
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

object AndroidGuidelinesServer {
    private const val SNIPPET_CHARS = 280
    private const val DEFAULT_LIMIT = 5
    private const val MAX_LIMIT = 20

    fun build(sections: List<Section>): Server {
        val all = Bm25Index(sections)
        val perSource: Map<SourceId, Bm25Index> =
            SourceId.values().associateWith { id ->
                Bm25Index(sections.filter { it.sourceId == id })
            }
        val byUri: Map<String, Section> = sections.associateBy { it.resourceUri }

        val server =
            Server(
                serverInfo =
                    Implementation(
                        name = "android-guidelines-mcp",
                        version = "0.1.0",
                    ),
                options =
                    ServerOptions(
                        capabilities =
                            ServerCapabilities(
                                tools = ServerCapabilities.Tools(listChanged = false),
                                resources = ServerCapabilities.Resources(subscribe = false, listChanged = false),
                            ),
                    ),
            )

        registerSearchTool(
            server,
            name = "search_kotlin_conventions",
            description = "Search the Kotlin Coding Conventions (JetBrains/kotlin-web-site) by keyword.",
            index = perSource[SourceId.KOTLIN_CONVENTIONS]!!,
        )
        registerSearchTool(
            server,
            name = "search_kotlin_style_migration",
            description = "Search the Kotlin Style Migration Guide (JetBrains/kotlin-web-site) by keyword.",
            index = perSource[SourceId.KOTLIN_STYLE_MIGRATION]!!,
        )
        registerSearchTool(
            server,
            name = "search_compose_api_guidelines",
            description = "Search the Jetpack Compose API Guidelines and Component API Guidelines (androidx) by keyword.",
            index =
                Bm25Index(
                    sections.filter {
                        it.sourceId == SourceId.COMPOSE_API || it.sourceId == SourceId.COMPOSE_COMPONENT_API
                    },
                ),
        )
        registerSearchTool(
            server,
            name = "search_java_style",
            description = "Search the Google Java Style Guide (google/styleguide) by keyword.",
            index = perSource[SourceId.JAVA_STYLE]!!,
        )
        registerSearchTool(
            server,
            name = "search_all_guidelines",
            description = "Search across all loaded Android / Kotlin / Java guidelines by keyword.",
            index = all,
        )

        for (section in sections) {
            server.addResource(
                uri = section.resourceUri,
                name = sectionResourceName(section),
                description = section.sectionPath.joinToString(" › "),
                mimeType = "text/markdown",
            ) { request ->
                val s = byUri[request.uri]
                ReadResourceResult(
                    contents =
                        listOf(
                            TextResourceContents(
                                text = renderSection(s),
                                uri = request.uri,
                                mimeType = "text/markdown",
                            ),
                        ),
                )
            }
        }

        return server
    }

    private fun registerSearchTool(
        server: Server,
        name: String,
        description: String,
        index: Bm25Index,
    ) {
        server.addTool(
            name = name,
            description = description,
            inputSchema =
                Tool.Input(
                    properties =
                        buildJsonObject {
                            putJsonObject("query") {
                                put("type", "string")
                                put("description", "Search keywords. English; symbols are split.")
                            }
                            putJsonObject("limit") {
                                put("type", "integer")
                                put("description", "Maximum number of hits to return. Default 5, capped at $MAX_LIMIT.")
                                put("minimum", 1)
                                put("maximum", MAX_LIMIT)
                            }
                        },
                    required = listOf("query"),
                ),
        ) { request ->
            val args = request.arguments
            val query =
                args
                    .get("query")
                    ?.jsonPrimitive
                    ?.content
                    ?.takeIf { it.isNotBlank() }
                    ?: return@addTool CallToolResult(
                        content = listOf(TextContent("query is required")),
                        isError = true,
                    )
            val limit =
                args
                    .get("limit")
                    ?.jsonPrimitive
                    ?.content
                    ?.toIntOrNull()
                    ?.coerceIn(1, MAX_LIMIT)
                    ?: DEFAULT_LIMIT
            val hits = index.search(query, limit)
            CallToolResult(
                content = listOf(TextContent(renderHits(query, hits))),
                isError = false,
            )
        }
    }

    private fun renderHits(
        query: String,
        hits: List<SearchHit>,
    ): String {
        if (hits.isEmpty()) return "No matches for query: \"$query\""
        return buildString {
            appendLine("Found ${hits.size} match(es) for \"$query\":")
            appendLine()
            for ((i, hit) in hits.withIndex()) {
                val s = hit.section
                appendLine("${i + 1}. ${s.title}  (score=${"%.2f".format(hit.score)})")
                appendLine("   source: ${s.sourceId.displayName}")
                if (s.sectionPath.size > 1) {
                    appendLine("   path: ${s.sectionPath.joinToString(" › ")}")
                }
                appendLine("   uri: ${s.resourceUri}")
                appendLine("   ${snippet(s.body)}")
                appendLine()
            }
        }.trimEnd()
    }

    private fun renderSection(section: Section?): String {
        if (section == null) return "Section not found."
        return buildString {
            appendLine("# ${section.title}")
            appendLine()
            appendLine("_${section.sourceId.displayName} — ${section.sectionPath.joinToString(" › ")}_")
            appendLine()
            append(section.body)
        }
    }

    private fun snippet(body: String): String {
        val flat = body.replace(Regex("\\s+"), " ").trim()
        return if (flat.length <= SNIPPET_CHARS) flat else flat.substring(0, SNIPPET_CHARS) + "…"
    }

    private fun sectionResourceName(section: Section): String = "${section.sourceId.displayName}: ${section.title}"
}
