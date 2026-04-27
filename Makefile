.PHONY: help setup upgrade fetch build test run format lint clean

# Homebrew で入れた OpenJDK をデフォルト JAVA_HOME とする。
# 既に環境変数で JAVA_HOME がセットされていればそれを優先する。
JAVA_HOME ?= /opt/homebrew/opt/openjdk
export JAVA_HOME

GRADLE ?= gradle
KTLINT ?= ktlint
KT_GLOB ?= "src/**/*.kt"

# デフォルトターゲット - ヘルプの表示
help:
	@echo "利用可能なコマンド:"
	@echo "  make setup      - 開発環境をセットアップ（OpenJDK / Gradle / ktlint）"
	@echo "  make build      - shadowJar をビルド（build/libs/*-all.jar）"
	@echo "  make fetch      - [実行時に必要] 公式ガイドライン repo を .guidelines-data/ に取得（再実行で更新）"
	@echo "  make test       - ユニットテストを実行"
	@echo "  make run        - MCP サーバを stdio で起動（手動デバッグ用）"
	@echo "  make format     - ktlint でコードを自動整形"
	@echo "  make lint       - ktlint でコードのフォーマットを検査（修正なし）"
	@echo "  make upgrade    - 開発環境ツールをアップグレード"
	@echo "  make clean      - ビルド成果物を削除"
	@echo "  make help       - このヘルプを表示"

# 開発環境のセットアップ（OpenJDK + Gradle + ktlint）
# Homebrew パッケージが既に入っていればスキップする冪等構成
setup:
	@echo "開発環境をセットアップしています..."
	@which brew > /dev/null || (echo "Homebrewがインストールされていません。https://brew.sh を参照してください。" && exit 1)
	@if ! brew list --versions openjdk > /dev/null 2>&1; then \
		echo "OpenJDKをインストール中..."; \
		brew install openjdk; \
	else \
		echo "OpenJDKは既にインストール済み"; \
	fi
	@if ! which gradle > /dev/null 2>&1; then \
		echo "Gradleをインストール中..."; \
		brew install gradle; \
	else \
		echo "Gradleは既にインストール済み"; \
	fi
	@if ! which ktlint > /dev/null 2>&1; then \
		echo "ktlintをインストール中..."; \
		brew install ktlint; \
	else \
		echo "ktlintは既にインストール済み"; \
	fi
	@echo "JAVA_HOME=$$JAVA_HOME"
	@"$$JAVA_HOME"/bin/java --version
	@$(GRADLE) --version | head -3
	@echo "セットアップが完了しました！"
	@echo ""
	@echo "次のステップ:"
	@echo "  1. 'make build' で shadowJar をビルドする"
	@echo "  2. 'make fetch' でガイドラインデータを取得する（実行時に必要、初回 + 更新したいとき）"
	@echo "  3. README の mcpServers サンプルに従って Claude Desktop / Code に登録する"

# ガイドライン repo を .guidelines-data/ に取得（再実行で最新へ更新）
fetch:
	@./scripts/fetch-guidelines.sh

# リリース用 fat JAR をビルド
build:
	@$(GRADLE) shadowJar

# ユニットテスト
test:
	@$(GRADLE) test

# stdio でサーバを起動（Claude 経由ではなくシェル単体での疎通確認用）
run:
	@$(GRADLE) run --console=plain --quiet

# ktlint でフォーマット適用
format:
	@which ktlint > /dev/null 2>&1 || (echo "ktlintがインストールされていません。'make setup' を実行してください。" && exit 1)
	$(KTLINT) -F $(KT_GLOB)

# ktlint でフォーマット検査（CI 用に修正なしで失敗させたい場合）
lint:
	@which ktlint > /dev/null 2>&1 || (echo "ktlintがインストールされていません。'make setup' を実行してください。" && exit 1)
	$(KTLINT) $(KT_GLOB)

# 開発環境ツールのアップグレード
upgrade:
	@echo "開発環境ツールをアップグレードしています..."
	@which brew > /dev/null 2>&1 || (echo "Homebrewがインストールされていません。" && exit 1)
	@if brew list --versions openjdk > /dev/null 2>&1; then \
		echo "OpenJDKをアップグレード中..."; \
		brew upgrade openjdk || true; \
	else \
		echo "OpenJDKがインストールされていません。'make setup' を実行してください。"; \
	fi
	@if which gradle > /dev/null 2>&1; then \
		echo "Gradleをアップグレード中..."; \
		brew upgrade gradle || true; \
	else \
		echo "Gradleがインストールされていません。'make setup' を実行してください。"; \
	fi
	@if which ktlint > /dev/null 2>&1; then \
		echo "ktlintをアップグレード中..."; \
		brew upgrade ktlint || true; \
	else \
		echo "ktlintがインストールされていません。'make setup' を実行してください。"; \
	fi
	@echo "アップグレードが完了しました！"

clean:
	@$(GRADLE) clean
