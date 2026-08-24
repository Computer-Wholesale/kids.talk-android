#!/usr/bin/env bash
set -euo pipefail

repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$repo_root"

create_test_secret() {
  od -An -N 24 -tx1 /dev/urandom | tr -d ' \n'
}

keystore_path=$(mktemp /tmp/kid267-ephemeral-upload-key-XXXXXX.jks)
rm -f "$keystore_path"
store_password=$(create_test_secret)
key_password=$(create_test_secret)
key_alias=kid267_ephemeral_upload_alias

cleanup() {
  rm -f "$keystore_path"
  unset store_password key_password
}
trap cleanup EXIT

keytool \
  -genkeypair \
  -keystore "$keystore_path" \
  -storepass "$store_password" \
  -alias "$key_alias" \
  -keypass "$key_password" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 1 \
  -dname "CN=KID-267 Ephemeral Test,O=Kids.Talk,C=AU" \
  >/dev/null 2>&1

KIDSTALK_UPLOAD_KEYSTORE_PATH="$keystore_path" \
KIDSTALK_UPLOAD_KEYSTORE_PASSWORD="$store_password" \
KIDSTALK_UPLOAD_KEY_ALIAS="$key_alias" \
KIDSTALK_UPLOAD_KEY_PASSWORD="$key_password" \
./gradlew --no-daemon --max-workers=1 -Dorg.gradle.jvmargs='-Xmx2048m' \
  :app:kidstalkReleaseSigningPreflight \
  :app:bundleRelease

if [ -z "$(find app/build/outputs/bundle/release -maxdepth 1 -type f -name '*.aab' -print -quit)" ]; then
  echo "KID267_EPHEMERAL_SIGNER_FAIL: release bundle was not produced" >&2
  exit 1
fi

echo "KID267_EPHEMERAL_SIGNER_TESTS=PASS"
