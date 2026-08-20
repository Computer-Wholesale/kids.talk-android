#!/usr/bin/env bash
# Executes one API-35 KID-394 emulator attempt. Exit code 75 is reserved for
# emulator infrastructure loss after the test command starts.
set -u -o pipefail

attempt="${1:?attempt name is required}"
workspace="${GITHUB_WORKSPACE:-$PWD}"
evidence_root="$workspace/artifacts/kid394-emulator"
diagnostics_dir="$evidence_root/diagnostics/$attempt"
gradle_log="$evidence_root/gradle/$attempt-connectedDebugAndroidTest.log"
scale_evidence_dir="$evidence_root/scale-evidence/$attempt"
status_file="$evidence_root/$attempt.exit-code"

mkdir -p "$diagnostics_dir" "$(dirname "$gradle_log")" "$scale_evidence_dir"

original_font=""
original_density=""
settings_captured=false
test_started=false

capture_diagnostics() {
    set +e
    adb devices -l > "$diagnostics_dir/adb-devices.txt" 2>&1
    adb shell getprop > "$diagnostics_dir/getprop.txt" 2>&1
    adb shell getprop sys.boot_completed > "$diagnostics_dir/boot-property.txt" 2>&1
    adb emu avd name > "$diagnostics_dir/avd-name.txt" 2>&1
    adb shell getprop ro.kernel.qemu.avd_name > "$diagnostics_dir/avd-property.txt" 2>&1
    adb logcat -d -v threadtime > "$diagnostics_dir/logcat.txt" 2>&1
    adb shell wm density > "$diagnostics_dir/density-after-test.txt" 2>&1
    adb shell settings get system font_scale > "$diagnostics_dir/font-scale-after-test.txt" 2>&1
    find "$HOME/.android/avd" -maxdepth 2 -name config.ini -print -exec cat {} \; > "$diagnostics_dir/avd-config.txt" 2>&1
}

export_scale_evidence() {
    set +e
    adb pull /sdcard/kid394-scale-evidence "$scale_evidence_dir" > "$diagnostics_dir/scale-evidence-pull.txt" 2>&1
}

capture_boot_preflight() {
    set +e
    adb devices -l > "$diagnostics_dir/adb-devices-preflight.txt" 2>&1
    adb shell getprop sys.boot_completed > "$diagnostics_dir/boot-property-preflight.txt" 2>&1
    adb emu avd name > "$diagnostics_dir/avd-name-preflight.txt" 2>&1
    adb shell getprop ro.kernel.qemu.avd_name > "$diagnostics_dir/avd-property-preflight.txt" 2>&1
    find "$HOME/.android/avd" -maxdepth 2 -name config.ini -print -exec cat {} \; > "$diagnostics_dir/avd-config-preflight.txt" 2>&1
}

restore_settings() {
    set +e
    if [ "$settings_captured" != true ]; then
        return
    fi

    if [ -z "$original_font" ] || [ "$original_font" = "null" ]; then
        adb shell settings delete system font_scale
    else
        adb shell settings put system font_scale "$original_font"
    fi

    override_density="$(printf '%s\n' "$original_density" | awk '/Override density/ {print $3}')"
    if [ -n "$override_density" ]; then
        adb shell wm density "$override_density"
    else
        adb shell wm density reset
    fi

    adb shell settings get system font_scale > "$diagnostics_dir/font-scale-restored.txt" 2>&1
    adb shell wm density > "$diagnostics_dir/density-restored.txt" 2>&1
}

finish() {
    status=$?
    if [ "$status" -ne 0 ] && [ "$test_started" != true ]; then
        status=75
    fi
    capture_diagnostics
    export_scale_evidence
    restore_settings
    printf '%s\n' "$status" > "$status_file"
    exit "$status"
}
trap finish EXIT

if ! adb wait-for-device; then
    exit 75
fi
if [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; then
    exit 75
fi

capture_boot_preflight

original_font="$(adb shell settings get system font_scale | tr -d '\r')"
original_density="$(adb shell wm density | tr -d '\r')"
settings_captured=true
printf '%s\n' "$original_font" > "$diagnostics_dir/font-scale-before.txt"
printf '%s\n' "$original_density" > "$diagnostics_dir/density-before.txt"

adb shell settings put system font_scale 1.30
adb shell wm density 560
adb shell settings get system font_scale > "$diagnostics_dir/font-scale-configured.txt"
adb shell wm density > "$diagnostics_dir/density-configured.txt"

test_started=true
set +e
./gradlew :app:connectedDebugAndroidTest --no-daemon --stacktrace --console=plain 2>&1 | tee "$gradle_log"
test_status=${PIPESTATUS[0]}
set -e

if [ "$test_status" -eq 0 ]; then
    exit 0
fi

if ! adb get-state > /dev/null 2>&1 || [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; then
    exit 75
fi

exit "$test_status"
