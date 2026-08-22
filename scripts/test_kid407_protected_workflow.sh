#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
workflow_path="${repository_root}/.github/workflows/kid407-protected-firebase-binding.yml"

require_literal() {
  local literal="$1"
  if ! grep -Fq -- "${literal}" "${workflow_path}"; then
    printf 'Expected protected workflow literal not found: %s\n' "${literal}" >&2
    exit 1
  fi
}

if [ ! -f "${workflow_path}" ]; then
  printf 'KID-407 protected Firebase workflow is missing.\n' >&2
  exit 1
fi

require_literal 'environment: protected-uat'
require_literal 'KIDSTALK_FIREBASE_ANDROID_CLIENT_CONFIG'
require_literal 'KIDSTALK_FIREBASE_ANDROID_KEY_RESTRICTION_CONFIRMATION'
require_literal 'umask 077'
require_literal 'trap cleanup EXIT'
require_literal 'rm -f app/google-services.json'
require_literal 'scripts/kid407_verify_firebase_contract.py --repository-root . --mode supplied --config app/google-services.json'
require_literal ':app:processDebugGoogleServices'
require_literal ':app:processReleaseGoogleServices'
require_literal 'GENERATED_GOOGLE_SERVICES_RESOURCES_PRESENT=true'
require_literal 'REDACTED_UAT_BINDING_STATUS=PASS'
require_literal 'if: always()'
require_literal 'Do not upload app/google-services.json'

printf 'KID407_PROTECTED_WORKFLOW_TESTS=PASS\n'
