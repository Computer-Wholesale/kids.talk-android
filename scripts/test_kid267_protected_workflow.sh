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

step_section() {
  local step_name=$1
  awk -v step_name="$step_name" '
    $0 == "      - name: " step_name { capture = 1; next }
    capture && $0 ~ /^      - name: / { exit }
    capture { print }
  ' "$workflow"
}

require_step_literal() {
  local step_name=$1
  local literal=$2
  if ! step_section "$step_name" | grep -Fq "$literal"; then
    echo "KID267_PROTECTED_WORKFLOW_FAIL: step [$step_name] is missing [$literal]" >&2
    exit 1
  fi
}

require_step_absent_literal() {
  local step_name=$1
  local literal=$2
  if step_section "$step_name" | grep -Fq "$literal"; then
    echo "KID267_PROTECTED_WORKFLOW_FAIL: step [$step_name] must not contain [$literal]" >&2
    exit 1
  fi
}

count_secret_mapping() {
  local variable_name=$1
  grep -F "${variable_name}: \${{ secrets.${variable_name} }}" "$workflow" | wc -l | tr -d ' '
}

require_literal "workflow_dispatch:"
require_literal "refs/heads/release/kid407-uat"
require_literal "environment: protected-uat"
require_literal "KID267_RELEASE_SIGNING_INPUT_MISSING"
require_literal "jarsigner -verify -strict -certs"
require_literal "trap cleanup ERR"
require_literal "Remove ephemeral protected material"
require_absent_literal "pull_request:"
require_absent_literal "push:"

private_signer_variables=(
  KIDSTALK_UPLOAD_KEYSTORE_B64
  KIDSTALK_UPLOAD_KEYSTORE_PASSWORD
  KIDSTALK_UPLOAD_KEY_ALIAS
  KIDSTALK_UPLOAD_KEY_PASSWORD
)

for variable_name in "${private_signer_variables[@]}"; do
  if grep -Eq "^    ${variable_name}:" "$workflow"; then
    echo "KID267_PROTECTED_WORKFLOW_FAIL: private signer value [$variable_name] is job scoped" >&2
    exit 1
  fi
done

require_step_literal "Materialise and preflight protected upload-key inputs" "KIDSTALK_UPLOAD_KEYSTORE_B64: \${{ secrets.KIDSTALK_UPLOAD_KEYSTORE_B64 }}"
require_step_literal "Materialise and preflight protected upload-key inputs" "KIDSTALK_UPLOAD_KEYSTORE_PASSWORD: \${{ secrets.KIDSTALK_UPLOAD_KEYSTORE_PASSWORD }}"
require_step_literal "Materialise and preflight protected upload-key inputs" "KIDSTALK_UPLOAD_KEY_ALIAS: \${{ secrets.KIDSTALK_UPLOAD_KEY_ALIAS }}"
require_step_literal "Materialise and preflight protected upload-key inputs" "KIDSTALK_UPLOAD_KEY_PASSWORD: \${{ secrets.KIDSTALK_UPLOAD_KEY_PASSWORD }}"

require_step_literal "Build signed release bundle" "KIDSTALK_UPLOAD_KEYSTORE_PASSWORD: \${{ secrets.KIDSTALK_UPLOAD_KEYSTORE_PASSWORD }}"
require_step_literal "Build signed release bundle" "KIDSTALK_UPLOAD_KEY_ALIAS: \${{ secrets.KIDSTALK_UPLOAD_KEY_ALIAS }}"
require_step_literal "Build signed release bundle" "KIDSTALK_UPLOAD_KEY_PASSWORD: \${{ secrets.KIDSTALK_UPLOAD_KEY_PASSWORD }}"
require_step_absent_literal "Build signed release bundle" "KIDSTALK_UPLOAD_KEYSTORE_B64"

if [ "$(count_secret_mapping KIDSTALK_UPLOAD_KEYSTORE_B64)" != "1" ]; then
  echo "KID267_PROTECTED_WORKFLOW_FAIL: keystore base64 must be scoped only to materialisation" >&2
  exit 1
fi
for variable_name in KIDSTALK_UPLOAD_KEYSTORE_PASSWORD KIDSTALK_UPLOAD_KEY_ALIAS KIDSTALK_UPLOAD_KEY_PASSWORD; do
  if [ "$(count_secret_mapping "$variable_name")" != "2" ]; then
    echo "KID267_PROTECTED_WORKFLOW_FAIL: private signer value [$variable_name] must be scoped only to preflight and bundle steps" >&2
    exit 1
  fi
done

require_step_literal "Validate signed bundle and public certificate fingerprint" "KIDSTALK_UPLOAD_KEY_CERT_SHA256: \${{ vars.KIDSTALK_UPLOAD_KEY_CERT_SHA256 }}"
for variable_name in "${private_signer_variables[@]}"; do
  require_step_absent_literal "Validate signed bundle and public certificate fingerprint" "$variable_name"
  require_step_absent_literal "Upload restricted release provenance and artifacts" "$variable_name"
done
require_step_absent_literal "Upload restricted release provenance and artifacts" "KIDSTALK_UPLOAD_KEY_CERT_SHA256"

require_step_absent_literal "Materialise and preflight protected upload-key inputs" "GITHUB_OUTPUT"
require_absent_literal "GITHUB_ENV"
require_literal "BUNDLETOOL_VERSION: \"1.18.0\""
require_literal "BUNDLETOOL_SHA256: \"78343764d2e79c8f55710378b04981fcb1e46daebfc3b5dc577778082e6a98fd\""
require_step_literal "Validate signed bundle and public certificate fingerprint" "sha256sum --check"
require_step_literal "Validate signed bundle and public certificate fingerprint" 'java -jar "$RUNNER_TEMP/bundletool.jar" validate --bundle'

checksum_line=$(grep -nF "sha256sum --check" "$workflow" | head -n 1 | cut -d: -f1)
java_line=$(grep -nF 'java -jar "$RUNNER_TEMP/bundletool.jar" validate --bundle' "$workflow" | head -n 1 | cut -d: -f1)
if [ -z "$checksum_line" ] || [ -z "$java_line" ] || [ "$checksum_line" -ge "$java_line" ]; then
  echo "KID267_PROTECTED_WORKFLOW_FAIL: bundletool checksum verification must precede Java execution" >&2
  exit 1
fi

echo "KID267_PROTECTED_WORKFLOW_TESTS=PASS"
