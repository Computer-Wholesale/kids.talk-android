# KID-269 Candidate SDK Inventory

**Status:** Green current-main evidence; final-candidate posture remains unresolved.
**Android source baseline:** `origin/main` `669d70a9885efb89ddf014be7944319ecce86e17`.
**Evidence branch / Red baseline:** `feature/KID-269-candidate-data-posture` / `67d58bc56fef5c6e431ec240283b5d8534bbb31a`.
**Scope:** Firebase configuration/plugins, Firebase Crashlytics, Firebase Cloud Messaging, and current-source third-party SDKs with direct authentication or calling functionality. This inventory does not establish release-candidate behaviour, a final Crashlytics disposition, or a Play declaration.

| Candidate artifact status | Evidence state |
| --- | --- |
| Candidate artifact status | **Not supplied.** No signed AAB/APK, signer, SHA-256, installed versionCode, device observation, or Play-track result is available. Candidate-specific fields below remain unresolved. |

## Evidence rules and limitations

The exact final signed AAB and observed device/Play-track behaviour are authoritative when available. Current-main source and locally generated Gradle outputs are interim evidence only. The local Gradle environment ran without `app/google-services.json`; it reported that Firebase Cloud Messaging and Crashlytics build activation were disabled. This is an environment observation, not proof of the intended candidate configuration. The release manifest task did not complete because `org.linphone:linphone-sdk-android:5.6.0-alpha.28+a101d0cb15` could not be resolved, so no merged release manifest is available.

This ledger contains no client configuration values, API keys, service-account material, registration tokens, credentials, raw device identifiers, or raw sensitive logs. A Firebase Android client configuration is not a server sender credential; KID-283 sender delivery and production background-wake behaviour remain out of scope.

## Component: Google Services Gradle Plugin

| Field | Evidence |
| --- | --- |
| Package / version | Plugin `com.google.gms.google-services`, version `4.5.0` in `gradle/libs.versions.toml`. |
| Compile / runtime inclusion | Build-time plugin only; not an Android runtime library. It is declared in the version catalog and applied programmatically from `app/build.gradle.kts`. |
| Release activation condition | `firebaseCloudMessagingAvailable = googleServices.exists()`; the plugin is applied only when `app/google-services.json` exists. Current checkout does not track that file. |
| Initialization / manifest proof | No generated release manifest is available because `:app:processReleaseManifest` failed during Linphone SDK resolution. Static source therefore cannot prove final generated Firebase provider/component state. |
| Candidate disposition | **UNRESOLVED.** Exact candidate artifact and its protected client-binding evidence have not been supplied. |
| Proof source | `app/build.gradle.kts` lines 19–30; `gradle/libs.versions.toml` lines 4 and 88; `git ls-files --error-unmatch app/google-services.json` returned absent on this checkout; KID-407 owns protected binding evidence. |
| Data categories / purposes | No runtime data purpose is asserted for the plugin itself. It can generate Firebase Android resources when activated; downstream runtime SDK assessment belongs to the component rows below. |
| Data Safety owner / effect | KID-264 owns Data Safety mapping. This row supplies an activation-condition fact only and makes no declaration. |
| Privacy-policy owner / effect | KID-265 owns policy prose. No policy statement is supplied by this build-plugin row. |
| Uncertainty | Candidate may be supplied through KID-407’s protected path rather than this checkout; the final candidate’s generated resources and manifest must be inspected. |

## Component: Firebase Crashlytics NDK

| Field | Evidence |
| --- | --- |
| Package / version | `com.google.firebase:firebase-crashlytics-ndk`; Firebase BoM `34.15.0`. In this local Gradle resolution, `releaseCompileClasspath` selected `firebase-crashlytics-ndk:20.0.6` and `firebase-crashlytics:20.0.6`. |
| Compile / runtime inclusion | Source conditionally uses `implementation(libs.google.firebase.crashlytics)` when `crashlyticsAvailable`; otherwise it uses `compileOnly(libs.google.firebase.crashlytics)`. Local `releaseCompileClasspath` includes the NDK artifact, but `releaseRuntimeClasspath` reported no matching Crashlytics dependency. |
| Release activation condition | `crashlyticsAvailable` requires `app/google-services.json`, `LinphoneSdkBuildDir/libs/`, and `LinphoneSdkBuildDir/libs-debug/`. When true, the build applies the Crashlytics plugin, enables native-symbol configuration for release, and finalizes release assembly/package tasks with symbol-upload tasks. |
| Initialization / manifest proof | `CoreContext.run()` calls `FirebaseCrashlytics.getInstance()` and registers the logging listener only when `BuildConfig.CRASHLYTICS_ENABLED` is true. The listener forwards `domain`, level, and message to `FirebaseCrashlytics.log(...)` only when both source gates are true. No merged release manifest was produced. |
| Candidate disposition | **UNRESOLVED.** The exact candidate AAB and its activation inputs are unavailable; KID-269 does not assert included, inactive, disabled, or removed. |
| Proof source | `app/build.gradle.kts` lines 18–35, 201–235, 286–292, and 361–376; `CoreContext.kt` lines 594–644; `./gradlew :app:dependencyInsight --dependency firebase-crashlytics --configuration releaseCompileClasspath` passed with version `20.0.6`; corresponding release-runtime insight passed with no matching dependency. |
| Data categories / purposes | Source indicates a potential diagnostics purpose only: log `domain`, level, and message can be forwarded if the source gates are true. Message content classification is unresolved; no data category or collection claim is made for the candidate. |
| Data Safety owner / effect | KID-264 must determine the exact-candidate Data Safety mapping after candidate evidence. This row is not a declaration and must not be read as `not collected`. |
| Privacy-policy owner / effect | KID-265 must determine whether policy wording is required after KID-264 mapping and exact-candidate evidence. |
| Uncertainty | Protected client config, local Linphone directories, actual release `BuildConfig`, generated manifest, runtime auto-initialization, final artifact composition, and historical release behaviour remain unproven. |

## Component: Firebase Cloud Messaging

| Field | Evidence |
| --- | --- |
| Package / version | `com.google.firebase:firebase-messaging`; Firebase BoM `34.15.0`. Local `releaseRuntimeClasspath` selected `firebase-messaging:25.1.0`. |
| Compile / runtime inclusion | `implementation(libs.google.firebase.messaging)` is unconditional in current source. Local release-runtime dependency insight includes version `25.1.0`. |
| Release activation condition | The library dependency is unconditional; application of the Google Services plugin is conditional on an existing `app/google-services.json`. Candidate client-binding evidence belongs to KID-407. |
| Initialization / manifest proof | Static application manifest declares `org.linphone.core.tools.firebase.FirebaseMessaging` for `com.google.firebase.MESSAGING_EVENT`. `HelpViewModel.kt` calls `FirebaseApp.getInstance()`. Generated release manifest evidence is unavailable because the manifest task failed during unrelated Linphone SDK resolution. |
| Candidate disposition | **UNRESOLVED.** Library resolution in this environment is not exact-candidate proof, and no claim is made about receiver registration, message delivery, wake behaviour, or data collection. |
| Proof source | `app/build.gradle.kts` lines 19–30 and 286–287; `app/src/main/AndroidManifest.xml` lines 164–170; `HelpViewModel.kt` lines 157–159; `./gradlew :app:dependencyInsight --dependency firebase-messaging --configuration releaseRuntimeClasspath` passed with version `25.1.0`. |
| Data categories / purposes | Static source supports only a narrow potential purpose: Firebase messaging integration. Message payload, identifiers, delivery behaviour, and collection categories are not classified here. |
| Data Safety owner / effect | KID-264 owns declaration mapping. This inventory neither declares data collected nor provides a basis for an FCM-wake claim. |
| Privacy-policy owner / effect | KID-265 owns policy wording. Any disclosure must await exact-candidate and KID-264 mapping evidence. |
| Uncertainty | Exact candidate binding, generated manifest, initialization state, sender path, registration-token handling, delivery, and background/locked-device behaviour are outside this evidence set. |

## Component: Linphone SDK Android

| Field | Evidence |
| --- | --- |
| Package / version | `org.linphone:linphone-sdk-android:5.6.0-alpha.28+a101d0cb15`. |
| Compile / runtime inclusion | Declared with `implementation(libs.linphone)`. Local release dependency report marked this artifact unresolved, and the release manifest task failed on the same artifact; current-source declaration is proven, final runtime inclusion is not. |
| Release activation condition | Unconditional source dependency. The build uses `LinphoneSdkBuildDir` for local `libs/` and `libs-debug/` directories when evaluating the separate Crashlytics condition. |
| Initialization / manifest proof | `CoreContext` creates and starts the Linphone Core; static source contains SIP account, call, messaging, media-export, and authentication paths. No exact candidate or generated manifest proves final runtime configuration. |
| Candidate disposition | **UNRESOLVED.** The source declares the SDK, but the current environment cannot resolve the release artifact and no final AAB exists. |
| Proof source | `gradle/libs.versions.toml` lines 35–38 and 77; `app/build.gradle.kts` lines 18–23 and 306; `CoreContext.kt` lines 240–272, 330–435, 484–541, and 657–695; release dependency report and `:app:processReleaseManifest` failure cite the unresolved coordinate. |
| Data categories / purposes | Current source indicates potential calling, messaging, media-file, SIP-account, and authentication functionality. Precise processing, destinations, and disclosure categories require candidate/deployment evidence and are not declared here. |
| Data Safety owner / effect | KID-264 owns Data Safety mapping. This row identifies functional surface only; it is not a data-practice declaration. |
| Privacy-policy owner / effect | KID-265 owns policy wording. This row does not make user-facing claims. |
| Uncertainty | Final SDK artifact, generated manifest, supplied provisioning, endpoint configuration, traffic handling, media behaviour, and candidate execution are unproven. TLS/SRTP or in-transit-encryption claims are intentionally excluded. |

## Component: AppAuth Android

| Field | Evidence |
| --- | --- |
| Package / version | `net.openid:appauth:0.11.1`. |
| Compile / runtime inclusion | Declared with `implementation(libs.openid.appauth)`; local `releaseRuntimeClasspath` includes version `0.11.1`. |
| Release activation condition | Unconditional build dependency. Runtime source reaches AppAuth only in the SSO flow, which is initiated in response to Bearer authentication handling. |
| Initialization / manifest proof | `SingleSignOnViewModel` imports and constructs `AuthorizationService`, fetches issuer configuration, creates authorization requests, and processes token requests. Exact-candidate manifest evidence is unavailable because the release manifest task failed. |
| Candidate disposition | **UNRESOLVED.** Local runtime dependency resolution is not exact-candidate proof; no claim is made about a configured issuer, actual sign-in, token storage, or account deletion. |
| Proof source | `gradle/libs.versions.toml` lines 34 and 75; `app/build.gradle.kts` line 304; `SingleSignOnActivity.kt` lines 31 and 113; `SingleSignOnViewModel.kt` lines 9–18, 161–222, and 272; `./gradlew :app:dependencyInsight --dependency appauth --configuration releaseRuntimeClasspath` passed with version `0.11.1`. |
| Data categories / purposes | Source indicates a potential SSO authentication purpose. Identifiers, authorization response content, tokens, issuer configuration, storage, and network destinations are not classified or declared in this ledger. |
| Data Safety owner / effect | KID-264 owns Data Safety mapping. No declaration is implied by this source inventory. |
| Privacy-policy owner / effect | KID-265 owns privacy-policy prose. Any applicable wording requires its own approved evidence path. |
| Uncertainty | Candidate issuer configuration, actual runtime invocation, token handling, network behaviour, and user-data categories are unproven. |

## Overlap consumption and handoff boundaries

| Related ticket | KID-269 inventory input or boundary | Owner |\n| --- | --- | --- |\n| KID-264 | Consume this factual SDK/activation ledger when mapping exact-candidate Data Safety declarations. | KID-264 |\n| KID-265 | Consume approved factual inventory inputs for any privacy-policy wording. | KID-265 |\n| KID-266 | Do not infer public listing claims from this ledger. | KID-266 |\n| KID-407 | Supply protected Firebase Android client-binding/package/signer and eventual artifact-binding evidence; no values are copied here. | KID-407 |\n| KID-413 | Independently test the exact signed and Play-delivered candidate; this ledger is not device or Play evidence. | KID-413 |\n| KID-414 | Own Play declaration/reviewer-access evidence; this ledger does not submit declarations. | KID-414 |\n| KID-267 | Supply signed AAB provenance, digest, versionCode and candidate identity for final reconciliation. | KID-267 |\n| KID-283 / KID-271 | Own production sender and mobile-calling wake/QA evidence; no FCM wake or background-call conclusion is made here. | KID-283 / KID-271 |

## Reproducible commands and results

| Command | Result | Meaning and limitation |
| --- | --- | --- |
| `bash scripts/test_kid269_candidate_data_posture.sh` | Red baseline failed as intended before inventory completion. | Proves missing required evidence fields are fail-closed. |
| `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` | Passed; the report marked Linphone SDK unresolved. | Build configuration can resolve enough to report dependencies, not an APK/AAB. |
| `./gradlew :app:dependencyInsight --dependency firebase-crashlytics --configuration releaseRuntimeClasspath` | Passed; no matching runtime Crashlytics dependency in this no-client-config environment. | Does not establish candidate composition. |
| `./gradlew :app:dependencyInsight --dependency firebase-crashlytics --configuration releaseCompileClasspath` | Passed; selected Crashlytics and Crashlytics NDK `20.0.6`. | Establishes local compile-classpath presence only. |
| `./gradlew :app:dependencyInsight --dependency firebase-messaging --configuration releaseRuntimeClasspath` | Passed; selected Firebase Messaging `25.1.0`. | Local runtime dependency evidence only. |
| `./gradlew :app:dependencyInsight --dependency appauth --configuration releaseRuntimeClasspath` | Passed; selected AppAuth `0.11.1`. | Local runtime dependency evidence only. |
| `./gradlew :app:testDebugUnitTest` | Failed. | Sandbox lacks Android SDK configuration: neither `ANDROID_HOME` nor `local.properties` `sdk.dir` is set. No unit tests executed. |
| `./gradlew :app:assembleRelease` | Failed. | The same missing Android SDK location blocked release task dependency resolution before signing or artifact generation. No APK/AAB was generated. |
| `bash scripts/test_kid407_firebase_contract.sh` | Passed: `KID407_FIREBASE_CONTRACT_TESTS=PASS`. | Existing redacted contract test passed; it does not prove protected candidate binding or sender delivery. |
| `./gradlew :app:processReleaseManifest` | Failed. | Blocked by unresolved Linphone SDK coordinate; no merged release manifest was generated. |

## Explicitly unsupported claims

This current-main evidence does not support claims that Crashlytics is included, active, disabled, removed, or data-free in the final candidate. It also does not support TLS/SRTP or in-transit-encryption claims, FCM sender delivery, registration-token collection, background/suspended/locked-device calling, full-screen calling, reviewer access, deletion, Play declaration completion, Play approval, or public-review readiness.

## References

[1]: `app/build.gradle.kts` at Android source baseline `669d70a9885efb89ddf014be7944319ecce86e17`.
[2]: `gradle/libs.versions.toml` at Android source baseline `669d70a9885efb89ddf014be7944319ecce86e17`.
[3]: `app/src/main/AndroidManifest.xml`, `CoreContext.kt`, `HelpViewModel.kt`, `SingleSignOnActivity.kt`, and `SingleSignOnViewModel.kt` at Android source baseline `669d70a9885efb89ddf014be7944319ecce86e17`.
[4]: Architect evidence-only execution approval in Linear KID-269 comment `30c632f0-0be5-48db-93ad-04bece61a269`.
