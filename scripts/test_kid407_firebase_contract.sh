#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
verifier="${repository_root}/scripts/kid407_verify_firebase_contract.py"
temporary_directory="$(mktemp -d)"
trap 'rm -rf "${temporary_directory}"' EXIT

valid_configuration="${temporary_directory}/valid-google-services.json"
invalid_configuration="${temporary_directory}/invalid-google-services.json"

cat > "${valid_configuration}" <<'JSON'
{
  "project_info": {
    "project_number": "506199291713",
    "project_id": "kids-talk-2fbbe"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:506199291713:android:testfixture",
        "android_client_info": {
          "package_name": "com.kidstalk.phone"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "test-key-not-real"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
JSON

cat > "${invalid_configuration}" <<'JSON'
{
  "project_info": {
    "project_number": "000000000000",
    "project_id": "linphone-android-8a563"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:000000000000:android:testfixture",
        "android_client_info": {
          "package_name": "org.linphone"
        }
      },
      "api_key": [
        {
          "current_key": "test-key-not-real"
        }
      }
    }
  ],
  "configuration_version": "1"
}
JSON

run_verifier() {
  python3 "${verifier}" --repository-root "${repository_root}" "$@"
}

require_output() {
  local expected="$1"
  local actual="$2"
  if ! grep -Fqx "${expected}" <<<"${actual}"; then
    printf 'Expected verifier output line not found: %s\n' "${expected}" >&2
    exit 1
  fi
}

no_fcm_output="$(run_verifier --mode no-fcm --config "${repository_root}/app/google-services.json")"
require_output 'CONFIG_MODE=NO_FCM' "${no_fcm_output}"
require_output 'TRACKED_CONFIGURATION=false' "${no_fcm_output}"
require_output 'IGNORED_CONFIGURATION=true' "${no_fcm_output}"
require_output 'UPSTREAM_FALLBACK=false' "${no_fcm_output}"

supplied_output="$(run_verifier --mode supplied --config "${valid_configuration}")"
require_output 'CONFIG_MODE=SUPPLIED' "${supplied_output}"
require_output 'FIREBASE_PROJECT_ID=kids-talk-2fbbe' "${supplied_output}"
require_output 'ANDROID_PACKAGE_NAME=com.kidstalk.phone' "${supplied_output}"
require_output 'FCM_SENDER_ID=506199291713' "${supplied_output}"
require_output 'CLIENT_MATCH_COUNT=1' "${supplied_output}"
require_output 'API_KEY_VALUE_REDACTED=true' "${supplied_output}"

if run_verifier --mode supplied --config "${invalid_configuration}" >"${temporary_directory}/invalid-output" 2>&1; then
  printf 'Expected upstream Firebase configuration to fail the KID-407 contract.\n' >&2
  exit 1
fi

if grep -Fq 'test-key-not-real' "${temporary_directory}/invalid-output"; then
  printf 'Verifier output must not expose an API key value.\n' >&2
  exit 1
fi

for generic_workflow in \
  "${repository_root}/.github/workflows/kid394-handoff-verification.yml" \
  "${repository_root}/.github/workflows/kid394-emulator.yml"; do
  if grep -Eq 'cat[[:space:]]+>[[:space:]]*app/google-services\.json|<<[[:space:]]*.*JSON' "${generic_workflow}"; then
    printf 'Generic verification workflow must not manufacture app/google-services.json: %s\n' "${generic_workflow}" >&2
    exit 1
  fi
done

printf 'KID407_FIREBASE_CONTRACT_TESTS=PASS\n'
