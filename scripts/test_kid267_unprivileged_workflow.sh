#!/usr/bin/env bash
set -euo pipefail

repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
workflow="$repo_root/.github/workflows/kid267-unprivileged-signing.yml"

if [ ! -f "$workflow" ]; then
  echo "KID267_UNPRIVILEGED_WORKFLOW_FAIL: hosted workflow is absent" >&2
  exit 1
fi

require_literal() {
  local literal=$1
  if ! grep -Fq -- "$literal" "$workflow"; then
    echo "KID267_UNPRIVILEGED_WORKFLOW_FAIL: missing [$literal]" >&2
    exit 1
  fi
}

require_absent_literal() {
  local literal=$1
  if grep -Fq -- "$literal" "$workflow"; then
    echo "KID267_UNPRIVILEGED_WORKFLOW_FAIL: forbidden [$literal]" >&2
    exit 1
  fi
}

require_literal "pull_request:"
require_literal "push:"
require_literal "- main"
require_literal "feature/KID-267-fail-closed-upload-signing"
require_literal "bash scripts/test_kid267_signing_contract.sh"
require_literal "bash scripts/test_kid267_protected_workflow.sh"
require_literal "bash scripts/test_kid267_unprivileged_workflow.sh"
require_literal "bash scripts/test_kid267_behavioral_preflight.sh"
require_literal "bash scripts/test_kid407_firebase_contract.sh"
require_literal "bash scripts/test_kid407_protected_workflow.sh"
require_literal "bash scripts/test_kid407_unprivileged_workflow.sh"
require_literal ":app:testDebugUnitTest"
require_literal ":app:compileDebugKotlin"
require_literal ":app:compileReleaseKotlin"
require_literal ":app:lintDebug"
require_literal ":app:lintRelease"
require_literal "sdk.dir=%s"
require_absent_literal "environment:"
require_absent_literal "secrets."
require_absent_literal "KIDSTALK_UPLOAD_"
require_absent_literal "actions/upload-artifact"
require_absent_literal "google-services.json"
require_absent_literal "protected-uat"

echo "KID267_UNPRIVILEGED_WORKFLOW_TESTS=PASS"
