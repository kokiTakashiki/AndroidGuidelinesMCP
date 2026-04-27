# AndroidGuidelinesMCP

Android / Kotlin / Java の公式スタイルガイド（Kotlin Coding Conventions、Kotlin Style Migration Guide、Compose API および Component API Guidelines、Google Java Style Guide）の **実テキスト** に基づいて回答を返すローカル MCP サーバです。

設計方針（BM25 の採用理由、ソース選定、ADR など）は [DESIGN.md](DESIGN.md) を参照してください。

## クイックスタート

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

| ターゲット | 用途 |
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

## ツール

| ツール | 対象範囲 |
| --- | --- |
| `search_kotlin_conventions(query, limit?)` | Kotlin Coding Conventions |
| `search_kotlin_style_migration(query, limit?)` | Kotlin Style Migration Guide |
| `search_compose_api_guidelines(query, limit?)` | Compose API + Component API Guidelines |
| `search_java_style(query, limit?)` | Google Java Style Guide |
| `search_all_guidelines(query, limit?)` | 上記すべての和集合 |

`limit` のデフォルトは 5、上限は 20 です。

各ヒットには、タイトル、階層的なセクションパス、BM25 スコア、スニペット、および `android-guidelines://<source-slug>/<anchor>` 形式のリソース URI が含まれ、MCP の `resources/read` フローで取得できます。

## 検索

BM25（k1 = 1.2、b = 0.75）を採用し、タイトルに 4 倍のブースト、英語向けの最小限のステミングを適用しています。リグレッション評価セットおよび現在のスコアは [docs/EVAL.md](docs/EVAL.md) を参照してください。

## 開発

基本的には `make` ターゲット経由での実行を推奨します。Gradle を直接叩きたい場合のショートカットは以下のとおりです:

```bash
gradle test                # tokenizer / BM25 / Markdown パーサのユニットテスト
gradle run                 # stdio で起動（.guidelines-data の準備が必要）
gradle shadowJar           # build/libs/android-guidelines-mcp-0.1.0-all.jar を生成
```

## 今後の予定

- top-1 命中率の改善（フレーズブースト / 深さブースト）
- developer.android.com スクレイピングのフォールバック（別バイナリ）
- Now in Android のコードインデックスローダー
- 日本語クエリ対応（kuromoji）
- 評価セットの拡充（30 クエリを目標）
