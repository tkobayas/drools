#!/bin/bash
#
# sync-main-ci.sh
#
# Syncs the fork's main-ci branch with upstream/main, then cherry-picks
# the "full CI in fork" commit (tkobayas-ci.yaml + this script) on top.
#
# The cherry-pick target is resolved by searching for the commit tagged with
# the marker message below — so this script stays self-consistent even after
# amends or rebases.
#
# Usage:
#   ./sync-main-ci.sh
#
# Requirements:
#   - remote "upstream" points to apache/incubator-kie-drools
#   - remote "origin"   points to tkobayas/drools (fork)
#   - run from the root of the repository

set -euo pipefail

CI_BRANCH="main-ci"
CI_COMMIT_MARKER="full CI in fork"   # grep target in git log --grep
UPSTREAM_REMOTE="upstream"
ORIGIN_REMOTE="origin"

# ---- Preflight checks -------------------------------------------------------

current_branch=$(git symbolic-ref --short HEAD)

if [[ -n "$(git status --porcelain)" ]]; then
  echo "ERROR: Working tree is dirty. Commit or stash changes first."
  exit 1
fi

# ---- Resolve CI commit hash -------------------------------------------------

echo "==> Resolving CI commit by message: \"$CI_COMMIT_MARKER\" ..."
CI_COMMIT=$(git log "$CI_BRANCH" --grep="$CI_COMMIT_MARKER" --fixed-strings --format="%H" -1)
if [[ -z "$CI_COMMIT" ]]; then
  echo "ERROR: Could not find a commit matching \"$CI_COMMIT_MARKER\" on $CI_BRANCH."
  exit 1
fi
echo "    Found: $CI_COMMIT"

# ---- Fetch ------------------------------------------------------------------

echo "==> Fetching $UPSTREAM_REMOTE/main ..."
git fetch "$UPSTREAM_REMOTE" main

# ---- Reset main-ci to upstream/main -----------------------------------------

echo "==> Switching to $CI_BRANCH ..."
git checkout "$CI_BRANCH"

echo "==> Resetting $CI_BRANCH to $UPSTREAM_REMOTE/main ..."
git reset --hard "$UPSTREAM_REMOTE/main"

# ---- Cherry-pick the CI commit ----------------------------------------------

echo "==> Cherry-picking $CI_COMMIT ..."
git cherry-pick "$CI_COMMIT"

# ---- Push -------------------------------------------------------------------

echo "==> Force-pushing $CI_BRANCH to $ORIGIN_REMOTE ..."
git push "$ORIGIN_REMOTE" "$CI_BRANCH" --force-with-lease

echo ""
echo "Done. $CI_BRANCH is now in sync with $UPSTREAM_REMOTE/main + CI yaml."

# ---- Return to previous branch (if we switched) ----------------------------

if [[ "$current_branch" != "$CI_BRANCH" ]]; then
  echo "==> Switching back to $current_branch ..."
  git checkout "$current_branch"
fi
