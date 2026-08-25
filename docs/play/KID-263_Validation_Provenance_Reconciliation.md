# KID-263 — Validation Provenance Reconciliation

**Branch:** `feature/KID-263-account-lifecycle-evidence`  
**Current Android main base:** `669d70a9885efb89ddf014be7944319ecce86e17`  
**SDK-capable validation commit:** `1ee5fd787268d1c18ebba68ac9b478cd96610e44`  
**Architect-cited branch tip:** `e397981da5f908d6b808542bc82d09bf64813f30`  
**Date:** 2026-08-25

## Reconciliation result

The SDK-capable Gradle commands and focused ledger validator executed at commit `1ee5fd787268d1c18ebba68ac9b478cd96610e44`. The subsequently cited branch tip `e397981da5f908d6b808542bc82d09bf64813f30` differs from that tested commit in **one documentation file only**: `docs/play/KID-263_Validation_Evidence.md`.

The reconciled Git checks established that there is **no non-document delta** between the two commits, and specifically no delta in `app/`, Gradle build configuration, the Gradle wrapper, or dependency declarations. A separate `git grep` check found no Gradle reference to `docs/play` at the tested commit. Therefore, the source and build configuration evaluated by the SDK-capable commands are identical between the tested commit and the cited branch tip.

| Check | Exact result |
| --- | --- |
| `git merge-base e397981… origin/main` | `669d70a9885efb89ddf014be7944319ecce86e17` |
| `git diff --name-status 1ee5fd7… e397981…` | `M docs/play/KID-263_Validation_Evidence.md` only |
| Non-document diff between the commits | Empty |
| Diff for `app`, Gradle configuration, Gradle wrapper, and dependencies | Empty |
| Gradle reference to `docs/play` at `1ee5fd7…` | None |

## What the SDK-capable commands actually established

At the validation commit, the focused account-lifecycle validator passed. `testDebugUnitTest` and `assembleDebug` advanced past Android-SDK discovery but stopped at dependency resolution because the pinned `org.linphone:linphone-sdk-android:5.6.0-alpha.28+a101d0cb15` artifact could not be resolved. `bundleRelease` stopped at KID-267 protected signing-input validation. The commands did not produce unit-test results, a debug artifact, or a release bundle.

The pinned artifact’s POM returned HTTP `404` from the currently configured official CI Maven repository. The repository configuration matches upstream Linphone Android’s documented strategy: resolve a locally built SDK when `LinphoneSdkBuildDir` points to a Maven repository, otherwise resolve from `https://download.linphone.org/maven_repository`.[1] The local property is empty in this environment. Search-index references to alternate release paths were not treated as evidence after direct artifact requests there also returned HTTP `404`.

## Bound conclusion

This reconciliation corrects the provenance mismatch without claiming a passing Android build. The SDK-capable validation results are applicable to the branch tip’s **unchanged Android source and build configuration**, but the branch remains blocked on an owner-provided exact local/CI Linphone SDK artifact. It also remains blocked on KID-267 protected signing inputs, KID-279’s authenticated canonical deletion flow and erasure proof, KID-413 candidate validation, and KID-264/KID-265/KID-414 reconciliation.

No dependency version, artifact group, signing configuration, secret, candidate, production system, or deletion flow was changed or accessed during this reconciliation.

## References

[1]: https://github.com/BelledonneCommunications/linphone-android/blob/master/settings.gradle.kts "Upstream Linphone Android settings.gradle.kts"
