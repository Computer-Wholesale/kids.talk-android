# KID-263 — Exact Linphone SDK Retrieval Blocker

**Branch:** `feature/KID-263-account-lifecycle-evidence`  
**Pinned application dependency:** `org.linphone:linphone-sdk-android:5.6.0-alpha.28+a101d0cb15`  
**Exact upstream SDK source revision:** `a101d0cb150a91854f28971ba41b3942237232ad`  
**Date:** 2026-08-25

## Objective

The architect directed that the pinned Linphone dependency be resolved in an SDK-capable environment before KID-263 Android validation is rerun. This report records the exact, bounded retrieval attempts and the remaining owner action required. It does **not** substitute another Linphone version, alter dependency declarations, access signing material, or create a candidate artifact.

## Completed environment preparation

A local Android SDK was installed outside the repository with Android API 37.0, Build Tools 36.0.0 and 37.0.0, Platform Tools, and NDK 27.3.13750724. Native prerequisites required for an Android Linphone SDK build were also installed locally: CMake, Ninja, NASM, YASM, Doxygen, Meson, Python Pystache, and Python Six. These changes did not alter the Android repository or its Gradle configuration.

The official `BelledonneCommunications/linphone-sdk` repository was cloned and detached at the exact revision embedded in the application’s pinned dependency. The upstream project identifies Android SDK generation as a source build that requires the repository’s exact submodules and an Android SDK/NDK.[1]

## Retrieval attempts and outcomes

| Attempt | Method | Outcome |
| --- | --- | --- |
| Existing CI Maven repository | Requested the exact pinned POM from `https://download.linphone.org/maven_repository`. | HTTP `404`; the current Maven metadata does not list the pinned version. |
| Alternate release-path verification | Direct requests for the exact POM and AAR at public Linphone release-path variants returned HTTP `404`. Search-index snippets were not accepted as artifact proof. | No trusted downloadable artifact found. |
| Exact source fallback | Cloned upstream SDK at `a101d0cb150a91854f28971ba41b3942237232ad`; invoked `git submodule update --init --recursive`. | Failed after retries because numerous required GitLab-hosted submodule clones terminated during the GnuTLS handshake. |
| Serialized HTTP/1.1 source fallback | Set the local external clone to HTTP/1.1 and retried submodule initialization serially (`--jobs 1`). | Failed on the same GitLab-hosted submodule retrievals with `gnutls_handshake() failed: The TLS connection was non-properly terminated`. |

The failure affects several independent exact upstream submodules, including `external/dav1d`, `external/decaf`, `external/mbedtls`, `external/openh264`, `external/opus`, and others. It prevents assembly of a faithful local SDK build in this environment. The source tree is intentionally not treated as buildable while required pinned submodules are unavailable.

## Required owner action

One of the following provenance-preserving inputs is needed before the Android unit/debug gates can proceed:

1. **Republish or provide the exact Maven artifact** for `5.6.0-alpha.28+a101d0cb15` from an authorised repository, including its POM and AAR.
2. **Provide an approved local Linphone SDK build directory** containing the exact Maven repository artifact, so `LinphoneSdkBuildDir` can be set only for validation execution.
3. **Restore reliable authorised access to all pinned upstream submodules** required to build the exact source revision.

A later Linphone version, a different artifact group/flavour, or an unverified mirror must not be substituted without an explicit architect-approved dependency-change scope and compatibility validation.

## Current KID-263 status

The focused lifecycle evidence validator remains Green. Android unit and debug builds remain blocked by the absent exact Linphone artifact. The release bundle additionally remains blocked by intentionally unavailable KID-267 protected signing inputs. Functional deletion remains unproven pending KID-279; exact candidate and cross-ticket gates remain open.

## References

[1]: https://github.com/BelledonneCommunications/linphone-sdk/tree/a101d0cb150a91854f28971ba41b3942237232ad "Linphone SDK source at the exact pinned revision"
