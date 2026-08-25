# KID-263 — Validation Evidence

**Branch:** `feature/KID-263-account-lifecycle-evidence`  
**Android base:** `669d70a9885efb89ddf014be7944319ecce86e17`  
**SDK-capable tested head:** `1ee5fd787268d1c18ebba68ac9b478cd96610e44`
**Execution date:** 2026-08-25

## Scope and safety posture

This record validates the KID-263 **current-source evidence ledger**, not functional account deletion or public-review readiness. No signing material, Firebase configuration, SIP credentials, customer/reviewer credentials, raw device identifiers, raw sensitive logs, candidate artifact, production system, or live deletion action was accessed. No Android source, Gradle configuration, dependency version, local project configuration, or historical PR was changed to bypass a blocker.

## Validation environment

A local Android SDK was installed outside the repository at `/home/ubuntu/android-sdk` using official Android command-line tools. The validation commands set `ANDROID_HOME` and `ANDROID_SDK_ROOT` only for their own process; `local.properties` was not created or changed. The installed packages include the Android API 37.0 platform, Build Tools 37.0.0, Platform Tools, and Gradle’s required Build Tools 36.0.0.

## Focused validation

| Command / check | Result | Evidence boundary |
| --- | --- | --- |
| `bash docs/play/validate-kid263-account-lifecycle.sh` | **PASS** | The ledger has every required lifecycle flow, source reference, shared-fact reconciliation entry, external gate, bounded conclusion, and release blocker. It does not demonstrate candidate or live behavior. |
| Current-source scan of Android `origin/main` controls and setup registration path | **PASS** | Confirms the restricted UI controls and registration delegation recorded in ledger references S1–S2. It does not establish credential persistence or app-account creation. |
| Current-source scan of portal activation, authentication, and GDPR routes at portal `origin/main` `81a0c86c32fa808f9280f5d8d46b08a21f950b90` | **PASS** | Confirms ledger references S3–S5. It does not prove a reachable canonical page, an end-to-end erasure action, or all retained data. |

## Android regression and build gates

| Command | Result | Exact blocker |
| --- | --- | --- |
| `ANDROID_HOME=/home/ubuntu/android-sdk ANDROID_SDK_ROOT=/home/ubuntu/android-sdk ./gradlew testDebugUnitTest --console=plain` | **BLOCKED / exit 1** | The Android SDK is available and Gradle began executing tasks, but `:app:dataBindingMergeDependencyArtifactsDebug` could not resolve `org.linphone:linphone-sdk-android:5.6.0-alpha.28+a101d0cb15`. No unit tests ran. |
| `ANDROID_HOME=/home/ubuntu/android-sdk ANDROID_SDK_ROOT=/home/ubuntu/android-sdk ./gradlew assembleDebug --console=plain` | **BLOCKED / exit 1** | The same unavailable pinned Linphone artifact blocked `:app:dataBindingMergeDependencyArtifactsDebug`; no debug artifact was produced. |
| `ANDROID_HOME=/home/ubuntu/android-sdk ANDROID_SDK_ROOT=/home/ubuntu/android-sdk ./gradlew bundleRelease --console=plain` | **BLOCKED / exit 1** | KID-267 protected-release signing input was intentionally unavailable. Gradle reported `KID267_RELEASE_SIGNING_INPUT_MISSING` for the upload keystore path, alias, and passwords. No signing material was requested, viewed, or supplied; no release bundle was produced. |

## Dependency-resolution evidence

The pinned artifact is not presently available from the configured public Linphone Maven repository. A direct request for its POM returned HTTP `404`, and the repository metadata does not contain `5.6.0-alpha.28+a101d0cb15` (it does include later alpha releases, which are not substitutes without an approved dependency-change scope). `LinphoneSdkBuildDir` is empty, so no locally built Maven repository is configured. This is a current dependency-provenance blocker requiring the appropriate Android/SDK owner’s direction; KID-263 must not alter the pinned dependency to manufacture a Green result.

## Candidate and functional-deletion status

The GitHub prerelease `v1.14` contains no release assets. KID-267 must supply the exact signed AAB identity, SHA-256, signer/provenance, versionName/versionCode, and installation identity. KID-279 remains responsible for the authenticated canonical deletion page, confirmation/error/retry behavior, end-to-end erasure result, and retention-completeness proof in a controlled deployed environment. KID-413 must independently validate the exact signed and Play-delivered candidate. KID-264, KID-265, and KID-414 must reconcile their assigned declarations, policy prose, and reviewer material with the ledger’s bounded conclusion.

## Conclusion

The focused KID-263 evidence gate is Green and the SDK-capable validation attempt is recorded. Android unit and debug builds remain **not passed** because the pinned Linphone artifact is unavailable; the release bundle remains **not passed** because protected KID-267 signing inputs are intentionally absent. Functional deletion and public-review readiness remain **not proven**.
