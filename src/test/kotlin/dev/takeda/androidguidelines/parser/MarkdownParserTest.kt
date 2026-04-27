package dev.takeda.androidguidelines.parser

import dev.takeda.androidguidelines.model.SourceId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkdownParserTest {
    @Test
    fun `splits by ATX headings and tracks hierarchy`() {
        val md =
            """
            # Coding conventions

            Intro paragraph.

            ## Naming rules

            Use camelCase.

            ### Functions

            Function names should be verbs.

            ## Formatting

            Indent with four spaces.
            """.trimIndent()

        val sections = MarkdownParser().parse(SourceId.KOTLIN_CONVENTIONS, md)
        val titles = sections.map { it.title }
        assertEquals(listOf("Coding conventions", "Naming rules", "Functions", "Formatting"), titles)

        val functions = sections.first { it.title == "Functions" }
        assertEquals(listOf("Coding conventions", "Naming rules", "Functions"), functions.sectionPath)
        assertTrue("verbs" in functions.body)
    }

    @Test
    fun `does not split inside code fences`() {
        val md =
            """
            # Section A

            ```kotlin
            // ## Not a heading
            fun f() {}
            ```

            ## Section B

            text
            """.trimIndent()

        val titles = MarkdownParser().parse(SourceId.KOTLIN_CONVENTIONS, md).map { it.title }
        assertEquals(listOf("Section A", "Section B"), titles)
    }

    @Test
    fun `strips trailing inline decoration in headings`() {
        val md =
            """
            ## Property naming {initial-collapse-state="collapsed"}

            body
            """.trimIndent()
        val s = MarkdownParser().parse(SourceId.KOTLIN_CONVENTIONS, md)
        assertEquals("Property naming", s.single().title)
    }
}
