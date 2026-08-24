#!/usr/bin/env bash
set -euo pipefail

repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$repo_root"

declare -a required_gradle_literals=(
  "kidstalkReleaseSigningPreflight"
  "KIDSTALK_UPLOAD_KEYSTORE_PATH"
  "KIDSTALK_UPLOAD_KEYSTORE_PASSWORD"
  "KIDSTALK_UPLOAD_KEY_ALIAS"
  "KIDSTALK_UPLOAD_KEY_PASSWORD"
  "KIDSTALK_UPLOAD_KEYSTORE_FILE"
  "isFile && rootProject.file(keystorePath).canRead()"
  "KID267_RELEASE_SIGNING_INPUT_MISSING"
)

for literal in "${required_gradle_literals[@]}"; do
  if ! grep -Fq "$literal" app/build.gradle.kts; then
    echo "KID267_SIGNING_CONTRACT_FAIL: missing Gradle signing adapter literal [$literal]" >&2
    exit 1
  fi
done

if [ ! -x scripts/test_kid267_behavioral_preflight.sh ]; then
  echo "KID267_SIGNING_CONTRACT_FAIL: executed behavioral preflight coverage is absent" >&2
  exit 1
fi

if git ls-files --error-unmatch keystore.properties >/dev/null 2>&1; then
  echo "KID267_SIGNING_CONTRACT_FAIL: root keystore.properties remains tracked" >&2
  exit 1
fi

for ignored_pattern in "/keystore.properties" "*.jks" "*.keystore"; do
  if ! grep -Fqx "$ignored_pattern" .gitignore; then
    echo "KID267_SIGNING_CONTRACT_FAIL: missing signing ignore rule [$ignored_pattern]" >&2
    exit 1
  fi
done

if ! grep -Fq "KIDSTALK_UPLOAD_KEYSTORE_PATH" keystore.properties.example 2>/dev/null; then
  echo "KID267_SIGNING_CONTRACT_FAIL: missing non-secret signing input example" >&2
  exit 1
fi

for behavioral_literal in ":app:bundleRelease" 'assert_no_release_outputs "$repo_root"' 'assert_no_release_outputs "$unprivileged_repo_root"'; do
  if ! grep -Fq "$behavioral_literal" scripts/test_kid267_behavioral_preflight.sh; then
    echo "KID267_SIGNING_CONTRACT_FAIL: behavioral coverage is missing [$behavioral_literal]" >&2
    exit 1
  fi
done
echo "KID267_SIGNING_CONTRACT_TESTS=PASS"
