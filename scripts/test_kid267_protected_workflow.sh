#!/usr/bin/env bash
set -euo pipefail

repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
workflow="$repo_root/.github/workflows/kid267-protected-release-signing.yml"

if [ ! -f "$workflow" ]; then
  echo "KID267_PROTECTED_WORKFLOW_FAIL: protected release workflow is absent" >&2
  exit 1
fi

require_literal() {
  local literal=$1
  if ! grep -Fq "$literal" "$workflow"; then
    echo "KID267_PROTECTED_WORKFLOW_FAIL: missing [$literal]" >&2
    exit 1
  fi
}

require_absent_literal() {
  local literal=$1
  if grep -Fq "$literal" "$workflow"; then
    echo "KID267_PROTECTED_WORKFLOW_FAIL: forbidden [$literal]" >&2
    exit 1
  fi
}

require_literal "workflow_dispatch:"
require_literal "refs/heads/release/kid407-uat"
require_literal "environment: protected-uat"
require_literal "KIDSTALK_UPLOAD_KEYSTORE_B64"
require_literal "KIDSTALK_UPLOAD_KEYSTORE_PASSWORD"
require_literal "KIDSTALK_UPLOAD_KEY_ALIAS"
require_literal "KIDSTALK_UPLOAD_KEY_PASSWORD"
require_literal "KIDSTALK_UPLOAD_KEY_CERT_SHA256"
require_literal "KID267_RELEASE_SIGNING_INPUT_MISSING"
require_literal "bundletool.jar\" validate --bundle"
require_literal "jarsigner -verify -strict -certs"
require_literal "trap cleanup ERR"
require_literal "Remove ephemeral protected material"
require_absent_literal "pull_request:"
require_absent_literal "push:"

echo "KID267_PROTECTED_WORKFLOW_TESTS=PASS"
