#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
workflow_path="${repository_root}/.github/workflows/kid407-unprivileged-contract.yml"

if [ ! -f "${workflow_path}" ]; then
  printf 'KID-407 unprivileged contract workflow is missing.\n' >&2
  exit 1
fi

require_literal() {
  local literal="$1"
  if ! grep -Fq -- "${literal}" "${workflow_path}"; then
    printf 'Expected unprivileged workflow literal not found: %s\n' "${literal}" >&2
    exit 1
  fi
}

require_literal 'pull_request:'
require_literal 'push:'
require_literal '      - main'
require_literal 'bash scripts/test_kid407_firebase_contract.sh'
require_literal 'bash scripts/test_kid407_protected_workflow.sh'
require_literal ':app:testDebugUnitTest'
require_literal ':app:compileDebugKotlin'
require_literal ':app:lint'

if grep -Eq 'protected-uat|KIDSTALK_FIREBASE_ANDROID_CLIENT_CONFIG|KIDSTALK_FIREBASE_ANDROID_KEY_RESTRICTION_CONFIRMATION|google-services\.json' "${workflow_path}"; then
  printf 'Unprivileged workflow must not reference protected UAT inputs or Firebase client configuration.\n' >&2
  exit 1
fi

printf 'KID407_UNPRIVILEGED_WORKFLOW_TESTS=PASS\n'
