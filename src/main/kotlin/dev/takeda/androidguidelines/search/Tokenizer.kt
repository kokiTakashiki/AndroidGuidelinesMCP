package dev.takeda.androidguidelines.search

object Tokenizer {
    private val splitter = Regex("[^a-z0-9]+")

    private val stopwords =
        setOf(
            "a",
            "an",
            "the",
            "is",
            "are",
            "be",
            "been",
            "being",
            "to",
            "of",
            "in",
            "on",
            "at",
            "for",
            "with",
            "by",
            "and",
            "or",
            "not",
            "but",
            "if",
            "then",
            "else",
            "this",
            "that",
            "these",
            "those",
            "it",
            "its",
            "as",
            "from",
            "into",
            "than",
            "so",
            "such",
            "we",
            "you",
            "they",
            "i",
            "he",
            "she",
        )

    fun tokenize(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        val lower = text.lowercase()
        return splitter
            .split(lower)
            .asSequence()
            .filter { it.isNotEmpty() }
            .filter { it !in stopwords }
            .map(::stem)
            .filter { it.isNotEmpty() }
            .toList()
    }

    private fun stem(token: String): String {
        if (token.length <= 3) return token
        return when {
            token.endsWith("ing") -> token.dropLast(3)
            token.endsWith("ies") -> token.dropLast(3) + "y"
            token.endsWith("ed") -> token.dropLast(2)
            token.endsWith("es") -> token.dropLast(2)
            token.endsWith("s") && !token.endsWith("ss") -> token.dropLast(1)
            else -> token
        }
    }
}
