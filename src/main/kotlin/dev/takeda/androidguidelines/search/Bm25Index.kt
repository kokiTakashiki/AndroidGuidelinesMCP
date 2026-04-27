package dev.takeda.androidguidelines.search

import dev.takeda.androidguidelines.model.Section
import kotlin.math.ln

data class SearchHit(
    val section: Section,
    val score: Double,
)

/**
 * BM25 index. Title boost is applied by repeating heading + sectionPath tokens
 * `titleBoost` times into the same field, instead of maintaining separate fields.
 */
class Bm25Index(
    sections: List<Section>,
    private val k1: Double = 1.2,
    private val b: Double = 0.75,
    private val titleBoost: Int = 4,
) {
    private data class Doc(
        val section: Section,
        val termFreqs: Map<String, Int>,
        val length: Int,
    )

    private val docs: List<Doc>
    private val docFreq: Map<String, Int>
    private val avgDocLen: Double
    private val docCount: Int

    init {
        docs =
            sections.map { section ->
                val titleTokens = Tokenizer.tokenize((listOf(section.title) + section.sectionPath).joinToString(" "))
                val bodyTokens = Tokenizer.tokenize(section.body)
                val tokens =
                    buildList {
                        repeat(titleBoost) { addAll(titleTokens) }
                        addAll(bodyTokens)
                    }
                Doc(
                    section = section,
                    termFreqs = tokens.groupingBy { it }.eachCount(),
                    length = tokens.size,
                )
            }
        docCount = docs.size
        avgDocLen = if (docCount == 0) 0.0 else docs.sumOf { it.length }.toDouble() / docCount
        val df = mutableMapOf<String, Int>()
        for (d in docs) {
            for (term in d.termFreqs.keys) {
                df[term] = (df[term] ?: 0) + 1
            }
        }
        docFreq = df
    }

    fun search(
        query: String,
        limit: Int = 5,
    ): List<SearchHit> {
        val queryTerms = Tokenizer.tokenize(query)
        if (queryTerms.isEmpty() || docs.isEmpty()) return emptyList()

        val results = ArrayList<SearchHit>(docs.size)
        for (doc in docs) {
            var score = 0.0
            for (term in queryTerms) {
                val tf = doc.termFreqs[term] ?: continue
                val df = docFreq[term] ?: continue
                val idf = ln((docCount - df + 0.5) / (df + 0.5) + 1.0)
                val denom = tf + k1 * (1 - b + b * doc.length / avgDocLen)
                score += idf * (tf * (k1 + 1)) / denom
            }
            if (score > 0) results += SearchHit(doc.section, score)
        }
        return results.sortedByDescending { it.score }.take(limit)
    }
}
