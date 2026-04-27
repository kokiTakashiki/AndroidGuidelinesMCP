# AndroidGuidelinesMCP

Local MCP server that returns answers grounded in **the actual text** of the Android / Kotlin / Java public style guides — Kotlin Coding Conventions, the Kotlin Style Migration Guide, the Compose API and Component API Guidelines, and the Google Java Style Guide.

See [DESIGN.md](DESIGN.md) for the design rationale (BM25 selection, source choices, ADR).

## Quick start

```bash
make setup    # OpenJDK / Gradle / ktlint を Homebrew 経由でインストール（冪等）
make build    # build/libs/android-guidelines-mcp-0.1.0-all.jar を生成
make fetch    # 公式ガイドライン repo を .guidelines-data/ に取得（初回 + 更新したいとき）
```

`make setup` 直後に `make build` まで通る状態になります。`make fetch` は **実行時に検索対象のドキュメントを引くため** のステップで、ビルド自体には不要。再実行で最新ブランチに追従します。

その後、Claude Desktop / Claude Code の `mcpServers` 設定に登録します:

```json
{
  "android-guidelines": {
    "command": "java",
    "args": ["-jar", "/absolute/path/to/build/libs/android-guidelines-mcp-0.1.0-all.jar"],
    "env": { "ANDROID_GUIDELINES_DATA": "/absolute/path/to/.guidelines-data" }
  }
}
```

`ANDROID_GUIDELINES_DATA` 未設定時は JVM の作業ディレクトリ配下の `./.guidelines-data` を見ます。

## make ターゲット

| Target | 用途 |
| --- | --- |
| `make setup` | OpenJDK / Gradle / ktlint を Homebrew でインストール（既存はスキップ） |
| `make fetch` | `scripts/fetch-guidelines.sh` を実行（再実行で最新ブランチに追従） |
| `make build` | shadowJar をビルド |
| `make test` | ユニットテスト |
| `make run` | stdio で MCP サーバを起動（手動疎通確認用） |
| `make format` | ktlint でコードを自動整形 |
| `make lint` | ktlint でフォーマット検査（CI 用） |
| `make upgrade` | Homebrew 経由のツールをアップグレード |
| `make clean` | ビルド成果物を削除 |

## Tools

| Tool | Scope |
| --- | --- |
| `search_kotlin_conventions(query, limit?)` | Kotlin Coding Conventions |
| `search_kotlin_style_migration(query, limit?)` | Kotlin Style Migration Guide |
| `search_compose_api_guidelines(query, limit?)` | Compose API + Component API Guidelines |
| `search_java_style(query, limit?)` | Google Java Style Guide |
| `search_all_guidelines(query, limit?)` | Union of all of the above |

`limit` defaults to 5, capped at 20.

Each hit returns title, hierarchical section path, BM25 score, snippet, and a resource URI of the form `android-guidelines://<source-slug>/<anchor>` that can be fetched via the MCP `resources/read` flow.

## Search

BM25 (k1 = 1.2, b = 0.75), with a 4× title boost and minimal English stemming. See [docs/EVAL.md](docs/EVAL.md) for the regression eval set and current scores.

## Development

`make` ターゲット越しを推奨。直接 Gradle を叩きたい場合のショートカットは以下:

```bash
gradle test                # unit tests for tokenizer / BM25 / Markdown parser
gradle run                 # run over stdio (requires .guidelines-data populated)
gradle shadowJar           # build/libs/android-guidelines-mcp-0.1.0-all.jar
```

## Future

- top-1 命中率 improvement (phrase boost / depth boost)
- developer.android.com scraping fallback (separate binary)
- Now in Android code index loader
- Japanese query support (kuromoji)
- Eval set expansion (target 30 queries)
