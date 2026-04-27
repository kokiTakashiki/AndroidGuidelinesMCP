package dev.takeda.androidguidelines.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenizerTest {
    @Test
    fun `splits on non-alphanumeric and lowercases`() {
        val tokens = Tokenizer.tokenize("Composable @Modifier hoisting!")
        assertTrue("composable" in tokens, "got $tokens")
        assertTrue("modifier" in tokens, "got $tokens")
        assertTrue("hoist" in tokens, "got $tokens")
    }

    @Test
    fun `removes stopwords`() {
        val tokens = Tokenizer.tokenize("a is the for")
        assertEquals(emptyList(), tokens)
    }

    @Test
    fun `stems suffix variants to common form`() {
        val a = Tokenizer.tokenize("hoisting")
        val b = Tokenizer.tokenize("hoisted")
        val c = Tokenizer.tokenize("hoists")
        assertEquals(a, b)
        assertEquals(a, c)
    }
}
