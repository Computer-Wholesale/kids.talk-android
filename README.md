# Kids.Talk Android

A supervised VoIP calling app for adult guardians and grandparents, built on [Linphone Android](https://github.com/BelledonneCommunications/linphone-android).

Kids.Talk is a locked-down SIP dialler designed for use by guardians and grandparents to communicate with the Kids.Talk physical handset. It provides a single large call button, guided permission setup, and no access to messaging, contacts, or call history. It connects to the Kids.Talk managed PBX service.

## Features

- Single-contact voice calling with one large Call button
- Guided first-run permission setup (microphone, notifications, full-screen alerts)
- Dark mode support following the system setting
- Ringer mode protection — never overrides silent/vibrate
- No camera, no contacts, no messaging, no call history
- Remote provisioning via the Kids.Talk PBX

## Licensing

This project is a fork of [linphone-android](https://github.com/BelledonneCommunications/linphone-android) and is distributed under the **GNU General Public License v3.0**. See [LICENSE.txt](LICENSE.txt) for the full licence text.

The upstream Linphone Android project is copyright © Belledonne Communications 2010-2025.  
Kids.Talk modifications are copyright © Computer Wholesale.

## Building

### Prerequisites

- Android Studio Hedgehog or later
- JDK 21
- Android SDK with API level 36 build tools

### Build

```bash
git clone https://github.com/Computer-Wholesale/kids.talk-android.git
cd kids.talk-android
./gradlew assembleDebug
```

The APK will be output to `app/build/outputs/apk/debug/`.

### Firebase client configuration

`app/google-services.json` is deliberately excluded from this repository. Normal non-production builds run without Firebase Cloud Messaging (FCM), which prevents an accidental fallback to the prior upstream Firebase project.

For a local non-production FCM experiment, obtain a **non-production** Firebase Console–downloaded Android client configuration from the Kids.Talk service operator and place it at `app/google-services.json` without staging it. Never copy a protected UAT configuration into the repository or commit it. The KID-407 verifier below confirms the required project, Android package, and sender identifiers while redacting API-key values:

```bash
python3 scripts/kid407_verify_firebase_contract.py --repository-root . --mode supplied --config app/google-services.json
```

Without a supplied local configuration, verify the intentional no-FCM path with:

```bash
python3 scripts/kid407_verify_firebase_contract.py --repository-root . --mode no-fcm --config app/google-services.json
```

Protected UAT configuration is materialised only by the protected CI workflow and is removed unconditionally after the job. It is not a developer bootstrap file.

### Release signing

Release bundles use the organisation-owned Kids.Talk **upload key** under Google Play App Signing. The upload key is not a debug, personal, guessed, temporary, or repository-stored identity. Its keystore, passwords, alias, public fingerprints, and recovery procedure are release-owner custody material and must never be placed in Git, local Gradle properties, build logs, issue comments, or artifacts.

Normal developer builds do not need signing inputs. Release packaging is fail-closed: `:app:kidstalkReleaseSigningPreflight` and release bundle tasks require all four protected input categories documented in [`keystore.properties.example`](keystore.properties.example). The protected release job alone materialises them ephemerally after its release-branch and environment approvals are configured. A missing or unreadable input stops before release bundle packaging and never falls back to debug or unsigned signing.

The real upload key is not generated or imported until the release owner has named the approved vault, primary custodian, independent recovery custodian, and recovery/rotation contact. The verified upload-key SHA-1 from the signed UAT artifact is later supplied to the Firebase owner for the direct-UAT Android-key restriction; Google Play app-signing SHA-1 is recorded separately after the first internal-test upload.

## Source publication

This repository constitutes the complete corresponding source code for all Kids.Talk Android releases, as required by GPL-3.0. Each tagged release corresponds to a published APK version.

| Tag | Version | Notes |
|-----|---------|-------|
| v1.14 | 1.14 | Current release |

## Privacy

Kids.Talk collects the minimum data necessary to operate a VoIP calling service. See the [Privacy Policy](https://kids.talk/privacy) for full details.

## Support

For service support, visit [kids.talk/help](https://kids.talk/help).
