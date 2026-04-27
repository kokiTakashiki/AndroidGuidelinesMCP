# Search Quality Evaluation

The retrieval quality of this MCP server is gated by a small **gold-standard query set** at [`eval-queries.json`](../eval-queries.json) — 10 queries that cover each source. The set is intentionally small and human-readable so we can keep it accurate as the upstream guidelines drift.

## Schema

Each entry is:

```json
{
  "query": "<keywords as the user would type them>",
  "expected_source": "<SourceId.slug>",
  "expected_anchor_contains": "<substring of the target section anchor>"
}
```

A hit counts as **correct** when the section's `sourceId.slug == expected_source` AND the section's `anchor` contains `expected_anchor_contains` (case-insensitive).

## Metrics

- **top-1 hit rate**: fraction of queries where the first BM25 result is correct.
- **top-3 hit rate**: fraction of queries where any of the first three results is correct.

## Result on the MVP set (DESIGN.md baseline)

| Metric | Score |
| --- | --- |
| top-1 | 6 / 10 = 60% |
| top-3 | 10 / 10 = 100% |

(Numbers come from the manual run referenced in DESIGN.md §"検索アルゴリズム / 評価結果". A scripted evaluator will be added in a follow-up; until then, run a search for each query and check by hand.)

## When to update the eval set

- An upstream guideline added or renamed a section that one of these queries hits → update `expected_anchor_contains`.
- A new source was added (e.g. nowinandroid, Android Kotlin Style Guide) → add at least 2 queries for it.
- A real user query failed to find an obvious answer → add it as a regression.

## Why not 30 queries on day 1

Small, calibrated > big, noisy. 10 queries is enough to detect a regression in the BM25 parameters or the parser; we'd rather extend it carefully than start with 30 queries half of which we can't keep accurate.
