#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
inventory_path="${repository_root}/docs/Developer briefs/KID-269_candidate_sdk_inventory.md"
build_file="${repository_root}/app/build.gradle.kts"
manifest_path="${repository_root}/app/src/main/AndroidManifest.xml"
version_catalog="${repository_root}/gradle/libs.versions.toml"

failures=0

fail() {
  printf 'KID269_VALIDATION_ERROR: %s\n' "$1" >&2
  failures=$((failures + 1))
}

require_source_literal() {
  local path="$1"
  local literal="$2"
  local label="$3"

  if ! grep -Fq -- "${literal}" "${path}"; then
    fail "Expected current-main evidence is absent for ${label}: ${path#"${repository_root}"/}"
  fi
}

require_inventory_field() {
  local component="$1"
  local field="$2"
  local section

  section="$(awk -v component="${component}" '
    $0 == "## Component: " component { inside = 1; next }
    inside && /^## Component: / { exit }
    inside { print }
  ' "${inventory_path}")"

  if [[ -z "${section}" ]]; then
    fail "Missing inventory section for source component: ${component}"
    return
  fi

  if ! grep -Eq "^\\| ${field//\//\\/} \\| [^|[:space:]][^|]* \\|$" <<<"${section}"; then
    fail "Missing ${field} field for inventory component: ${component}"
  fi
}

require_inventory_component() {
  local component="$1"
  local field

  for field in \
    "Package / version" \
    "Compile / runtime inclusion" \
    "Release activation condition" \
    "Initialization / manifest proof" \
    "Candidate disposition" \
    "Proof source" \
    "Data categories / purposes" \
    "Data Safety owner / effect" \
    "Privacy-policy owner / effect" \
    "Uncertainty"; do
    require_inventory_field "${component}" "${field}"
  done
}

if [[ ! -f "${inventory_path}" ]]; then
  fail "Inventory document is missing: docs/Developer briefs/KID-269_candidate_sdk_inventory.md"
else
  if ! grep -Eq '^\| Candidate artifact status \| [^|[:space:]][^|]* \|$' "${inventory_path}"; then
    fail "Missing candidate artifact status; candidate disposition cannot be asserted without artifact evidence"
  fi

  if grep -Enqi '(api[_ -]?key[[:space:]]*[:=][[:space:]]*[^ <]|service_account[[:space:]]*[:=]|private_key[[:space:]]*[:=]|BEGIN (RSA |EC )?PRIVATE KEY|registration[_ -]?token[[:space:]]*[:=])' "${inventory_path}"; then
    fail "Inventory document contains a possible secret or raw sensitive identifier"
  fi
fi

require_source_literal "${build_file}" "val firebaseCloudMessagingAvailable = googleServices.exists()" "Firebase Messaging activation gate"
require_source_literal "${build_file}" "val crashlyticsAvailable = googleServices.exists() && linphoneLibs.exists() && linphoneDebugLibs.exists()" "Crashlytics activation gate"
require_source_literal "${build_file}" "implementation(libs.google.firebase.messaging)" "Firebase Messaging dependency"
require_source_literal "${build_file}" "implementation(libs.google.firebase.crashlytics)" "Crashlytics runtime dependency path"
require_source_literal "${build_file}" "compileOnly(libs.google.firebase.crashlytics)" "Crashlytics compile-only dependency path"
require_source_literal "${build_file}" "buildConfigField(\"Boolean\", \"CRASHLYTICS_ENABLED\", crashlyticsAvailable.toString())" "Crashlytics build configuration"
require_source_literal "${version_catalog}" "google-firebase-crashlytics = { group = \"com.google.firebase\", name = \"firebase-crashlytics-ndk\" }" "Crashlytics package declaration"
require_source_literal "${version_catalog}" "linphone = { group = \"org.linphone\", name = \"linphone-sdk-android\"" "Linphone SDK package declaration"
require_source_literal "${version_catalog}" "openid-appauth = { group = \"net.openid\", name = \"appauth\"" "AppAuth package declaration"
require_source_literal "${manifest_path}" "org.linphone.core.tools.firebase.FirebaseMessaging" "Firebase Messaging manifest service"

if [[ -f "${inventory_path}" ]]; then
  require_inventory_component "Google Services Gradle Plugin"
  require_inventory_component "Firebase Crashlytics NDK"
  require_inventory_component "Firebase Cloud Messaging"
  require_inventory_component "Linphone SDK Android"
  require_inventory_component "AppAuth Android"
fi

if (( failures > 0 )); then
  printf 'KID269_CANDIDATE_DATA_POSTURE=FAIL\n' >&2
  exit 1
fi

printf 'KID269_CANDIDATE_DATA_POSTURE=PASS\n'
