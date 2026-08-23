#!/usr/bin/env bash
# Executes one API-35 KID-394 emulator attempt. Exit code 75 is reserved for
# emulator infrastructure loss or an invalid hostile-scale test geometry.
set -u -o pipefail

attempt="${1:?attempt name is required}"
workspace="${GITHUB_WORKSPACE:-$PWD}"
evidence_root="$workspace/artifacts/kid394-emulator"
diagnostics_dir="$evidence_root/diagnostics/$attempt"
gradle_log="$evidence_root/gradle/$attempt-connectedDebugAndroidTest.log"
scale_evidence_dir="$evidence_root/scale-evidence/$attempt"
status_file="$evidence_root/$attempt.exit-code"
profile_name="Nexus 6"
minimum_logical_width_dp=320

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
    sdk_root="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
    "$sdk_root/emulator/emulator" -version > "$diagnostics_dir/emulator-version.txt" 2>&1
    sdkmanager_binary="$(find "$sdk_root/cmdline-tools" -type f -name sdkmanager -print -quit)"
    if [ -n "$sdkmanager_binary" ]; then
        "$sdkmanager_binary" --list > "$diagnostics_dir/sdk-packages.txt" 2>&1
    else
        printf '%s\n' 'sdkmanager not found after emulator setup' > "$diagnostics_dir/sdk-packages.txt"
    fi
}

record_geometry_preflight() {
    local reported_size reported_density effective_size effective_density width_px height_px width_dp height_dp

    reported_size="$(adb shell wm size | tr -d '\r')"
    reported_density="$(adb shell wm density | tr -d '\r')"
    effective_size="$(printf '%s\n' "$reported_size" | awk -F': ' '/Override size/ {print $2; found=1} END {if (!found) exit 1}')" || \
        effective_size="$(printf '%s\n' "$reported_size" | awk -F': ' '/Physical size/ {print $2; exit}')"
    effective_density="$(printf '%s\n' "$reported_density" | awk -F': ' '/Override density/ {print $2; found=1} END {if (!found) exit 1}')" || \
        effective_density="$(printf '%s\n' "$reported_density" | awk -F': ' '/Physical density/ {print $2; exit}')"

    width_px="${effective_size%x*}"
    height_px="${effective_size#*x}"
    if ! [[ "$width_px" =~ ^[0-9]+$ && "$height_px" =~ ^[0-9]+$ && "$effective_density" =~ ^[0-9]+$ ]] || [ "$effective_density" -le 0 ]; then
        {
            printf 'profile=%s\n' "$profile_name"
            printf 'font_scale=%s\n' "$(adb shell settings get system font_scale | tr -d '\r')"
            printf 'wm_size=%s\n' "$reported_size"
            printf 'wm_density=%s\n' "$reported_density"
            printf 'geometry_status=invalid\n'
        } > "$diagnostics_dir/geometry-preflight.txt"
        return 75
    fi

    width_dp="$(awk -v px="$width_px" -v density="$effective_density" 'BEGIN { printf "%.2f", px * 160 / density }')"
    height_dp="$(awk -v px="$height_px" -v density="$effective_density" 'BEGIN { printf "%.2f", px * 160 / density }')"
    {
        printf 'profile=%s\n' "$profile_name"
        printf 'font_scale=%s\n' "$(adb shell settings get system font_scale | tr -d '\r')"
        printf 'wm_size=%s\n' "$reported_size"
        printf 'wm_density=%s\n' "$reported_density"
        printf 'effective_width_px=%s\n' "$width_px"
        printf 'effective_height_px=%s\n' "$height_px"
        printf 'effective_density_dpi=%s\n' "$effective_density"
        printf 'logical_width_dp=%s\n' "$width_dp"
        printf 'logical_height_dp=%s\n' "$height_dp"
        printf 'minimum_logical_width_dp=%s\n' "$minimum_logical_width_dp"
    } > "$diagnostics_dir/geometry-preflight.txt"

    if awk -v width="$width_dp" -v minimum="$minimum_logical_width_dp" 'BEGIN { exit !(width >= minimum) }'; then
        printf 'geometry_status=valid\n' >> "$diagnostics_dir/geometry-preflight.txt"
        return 0
    fi

    printf 'geometry_status=below-minimum-logical-width\n' >> "$diagnostics_dir/geometry-preflight.txt"
    return 75
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

if ! record_geometry_preflight; then
    exit 75
fi

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
