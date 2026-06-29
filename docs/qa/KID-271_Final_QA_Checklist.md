# KID-271: Final QA Pass Checklist

This document provides the mandatory checklist for the QA Agent to execute against the final signed release build (v1.15) before Google Play submission.

> **Status: BLOCKED** — Cannot execute until the following are resolved:
> - **KID-267**: Production keystore generated and `./gradlew bundleRelease` passes (operator action required)
> - **KID-282**: Camera `uses-feature` and `FOREGROUND_SERVICE_CAMERA` stripped from manifest (unmerged)
> - **KID-283**: PBX FCM push notification configured (background incoming-call push not yet working)
> - All PRs merged to `main` in dependency order

**Pre-requisites:**
- All PRs (KID-261 through KID-282) must be merged into `main` in dependency order.
- The build must be an R8-obfuscated `release` build (`isMinifyEnabled=true`, `isShrinkResources=true`).
- The build must be tested on at least **two physical Android devices** (different OEMs preferred, e.g., Samsung + Pixel).
- The build must be signed with the production upload keystore (KID-267).
- Upgrade-install test: **N/A for first Play release** (cross-key upgrade from debug to production-signed is not possible on Android).

---

## 1. Brand & UI Compliance

| Test Case | Expected Result | Device 1 Pass/Fail | Device 2 Pass/Fail | Evidence Required |
| :--- | :--- | :--- | :--- | :--- |
| **App Icon** | Launcher icon is Sunshine Yellow with Kids.Talk logo. No Linphone branding. | | | Screenshot |
| **Splash Screen** | Sunshine Yellow background, Kids.Talk logo centered. | | | Screenshot |
| **About Screen** | Triggered via info icon. Zero border radius, hard black shadow, Sunshine Yellow version badge, Grass Green link rows. | | | Screenshot |
| **Privacy Policy row** | Tapping opens `https://www.kids.talk/privacy-policy` in external browser. | | | Screenshot |
| **Support row** | Tapping opens `https://www.kids.talk/support` in external browser. | | | Screenshot |
| **Manage Account row** | Tapping opens `https://www.kids.talk/account/manage` in external browser. | | | Screenshot |
| **GPL attribution** | "Built on Linphone Android · © Belledonne Communications · GPL-3.0 · source: github.com/..." visible in About screen. | | | Screenshot |
| **Typography** | About screen uses Space Grotesk for headings, IBM Plex Sans for body. | | | Screenshot |
| **Dark Mode** | App correctly follows system dark/light mode toggle on all screens (Call, Setup, About). | | | Screenshot (dark + light) |

---

## 2. Core Functionality (Bidirectional — via `pbx.kids.talk`)

| Test Case | Expected Result | Device 1 Pass/Fail | Device 2 Pass/Fail | Evidence Required |
| :--- | :--- | :--- | :--- | :--- |
| **Fresh Install** | App requests Microphone and Notification permissions. Does **NOT** request Camera. | | | Screenshot of permission dialogs |
| **Provisioning** | Login succeeds with valid Kids.Talk PBX credentials. SIP registration confirmed in logcat. | | | Logcat snippet |
| **Outgoing Call** | Call connects to a valid `pbx.kids.talk` endpoint. Two-way audio works. | | | Logcat snippet |
| **Incoming Call (Foreground)** | Call rings. Answering establishes two-way audio. | | | Logcat snippet |
| **Incoming Call (Background/Locked)** | Device wakes. Full-screen incoming call intent is displayed. Call can be answered. | | | Logcat snippet + screenshot |
| **Ringer Mode Integrity** | Ringer mode (Silent/Vibrate/Normal) is **NEVER** altered by the app during or after a call. Check before and after. | | | Logcat snippet (ringer mode state before/after) |

---

## 3. Compliance & Security

| Test Case | Expected Result | Device 1 Pass/Fail | Device 2 Pass/Fail | Evidence Required |
| :--- | :--- | :--- | :--- | :--- |
| **Camera Permission** | `android.permission.CAMERA` is completely absent from the merged manifest. Verify via `aapt dump permissions`. | | | `aapt dump permissions` output |
| **Camera FGS** | `FOREGROUND_SERVICE_CAMERA` is completely absent from the merged manifest. | | | `aapt dump` output |
| **Crashlytics** | App does not crash on startup. Network traffic inspection shows no calls to `crashlytics.com` or `firebase.com/crashlytics`. | | | Network log |
| **Account Deletion** | "Manage Account" button in About screen opens `https://www.kids.talk/account/manage` in external browser. | | | Screenshot |
| **Open Source Licences** | "Open-source licences" button in About screen opens the native OSS licences view (OssLicensesMenuActivity). | | | Screenshot |
| **Screenshots** | Taking a screenshot of the app is permitted (no `FLAG_SECURE` blocks). | | | Screenshot taken successfully |

---

## 4. R8/Minification Regression

| Test Case | Expected Result | Device 1 Pass/Fail | Device 2 Pass/Fail | Evidence Required |
| :--- | :--- | :--- | :--- | :--- |
| **SIP Registration (R8)** | SIP registration succeeds on the R8-obfuscated `release` build (not debug). Reflection paths not broken. | | | Logcat snippet |
| **Native Audio (R8)** | Audio works on the release build. JNI/native audio paths not broken by R8. | | | Call test evidence |

---

## 5. Play Store Screenshots (KID-266)

Once all tests pass, the QA Agent must capture the following screenshots for Play Store submission:

| Screenshot | Screen | Notes |
| :--- | :--- | :--- |
| Setup flow — Step 1 | Onboarding/provisioning screen | |
| Main call screen | Idle state | |
| About screen | With version badge visible | |
| Incoming call | Full-screen alert | |

---

## 6. Final Handoff

Once all tests pass:
1. Complete all Pass/Fail cells above with evidence attached.
2. Commit the completed checklist back to `docs/qa/KID-271_Final_QA_Checklist.md` in `kids.talk-android`.
3. Notify the Architect that the build is ready for Google Play submission.
4. Move KID-271 to `Done`.
