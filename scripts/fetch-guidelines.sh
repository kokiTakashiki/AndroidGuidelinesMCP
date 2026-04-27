#!/usr/bin/env bash
# Fetch the upstream guideline repositories into .guidelines-data/.
# Re-run this script to update to the latest branch HEAD.
#
# Usage:
#   scripts/fetch-guidelines.sh [data-dir]
#
# Defaults to .guidelines-data/ at the repo root.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DATA_DIR="${1:-${ROOT}/.guidelines-data}"

mkdir -p "${DATA_DIR}"

clone_or_pull_full() {
    local repo_url="$1"
    local target="$2"
    local branch="$3"
    if [ -d "${target}/.git" ]; then
        echo "[fetch] Updating ${target}"
        git -C "${target}" fetch --depth 1 origin "${branch}"
        git -C "${target}" checkout -B "${branch}" "origin/${branch}"
    else
        echo "[fetch] Cloning ${repo_url} (branch ${branch}) into ${target}"
        git clone --depth 1 --branch "${branch}" "${repo_url}" "${target}"
    fi
}

clone_or_pull_sparse() {
    local repo_url="$1"
    local target="$2"
    local branch="$3"
    local subpath="$4"
    if [ -d "${target}/.git" ]; then
        echo "[fetch] Updating sparse ${target}"
        git -C "${target}" fetch --depth 1 origin "${branch}"
        git -C "${target}" checkout -B "${branch}" "origin/${branch}"
    else
        echo "[fetch] Sparse-cloning ${repo_url} (branch ${branch}, path ${subpath}) into ${target}"
        git clone --depth 1 --filter=blob:none --sparse --branch "${branch}" "${repo_url}" "${target}"
        git -C "${target}" sparse-checkout set "${subpath}"
    fi
}

# 1) JetBrains/kotlin-web-site (master) — full clone, includes coding-conventions.md
clone_or_pull_full \
    "https://github.com/JetBrains/kotlin-web-site.git" \
    "${DATA_DIR}/kotlin-web-site" \
    "master"

# 2) androidx/androidx (androidx-main) — sparse-checkout compose/docs
clone_or_pull_sparse \
    "https://github.com/androidx/androidx.git" \
    "${DATA_DIR}/androidx" \
    "androidx-main" \
    "compose/docs"

# 3) google/styleguide (gh-pages) — full clone, includes javaguide.html
clone_or_pull_full \
    "https://github.com/google/styleguide.git" \
    "${DATA_DIR}/styleguide" \
    "gh-pages"

echo "[fetch] Done. Data is in ${DATA_DIR}"
