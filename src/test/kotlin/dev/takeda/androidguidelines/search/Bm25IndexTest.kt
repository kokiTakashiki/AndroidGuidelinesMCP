package dev.takeda.androidguidelines.search

import dev.takeda.androidguidelines.model.Section
import dev.takeda.androidguidelines.model.SourceId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Bm25IndexTest {
    private fun section(
        title: String,
        body: String,
        source: SourceId = SourceId.KOTLIN_CONVENTIONS,
    ) = Section(
        sourceId = source,
        sectionPath = listOf(title),
        anchor = title.lowercase().replace(' ', '-'),
        title = title,
        body = body,
    )

    @Test
    fun `returns top hits sorted by score`() {
        val index =
            Bm25Index(
                listOf(
                    section("Naming conventions", "Functions and properties must use lowerCamelCase."),
                    section("Composable functions", "A composable function emits UI as a side effect."),
                    section("Modifier hoisting", "Hoist modifier parameters so callers can pass them in."),
                ),
            )
        val hits = index.search("modifier hoisting")
        assertTrue(hits.isNotEmpty())
        assertEquals("Modifier hoisting", hits[0].section.title)
    }

    @Test
    fun `title matches outrank body matches via title boost`() {
        val index =
            Bm25Index(
                listOf(
                    section(
                        "State hoisting",
                        "Concept of pulling state up to the caller so the composable becomes stateless.",
                    ),
                    section(
                        "Generic naming",
                        "When picking names, prefer descriptive ones; avoid the word state where possible.",
                    ),
                ),
            )
        val hits = index.search("state hoisting")
        assertEquals("State hoisting", hits[0].section.title)
    }

    @Test
    fun `unrelated query returns no hits`() {
        val index = Bm25Index(listOf(section("Kotlin idioms", "Use data classes for value-type aggregates.")))
        val hits = index.search("kubernetes pod scheduling")
        assertEquals(emptyList(), hits)
    }
}
