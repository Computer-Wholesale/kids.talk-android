# Kids.Talk Android — Google Play Data Safety Mapping Document

This document provides the exact answers to be entered into the Google Play Console Data Safety form for the Kids.Talk Android app.

**Status:** Draft — pending KID-269 merge (Crashlytics removal). The Crashlytics row below reflects the post-KID-269 state. Finalize this document after KID-269 lands and all dependent tickets (KID-263, KID-265) are merged.

## Context and Dependencies

The Kids.Talk Android application operates under several architectural and policy constraints that dictate its Data Safety declarations. The target audience is strictly defined as adults (guardians and grandparents), explicitly excluding children as direct users. Consequently, compliance with the Google Play Families Policy is not required, and no children's data handling clauses are necessary.

Furthermore, the app has been heavily minimised to reduce data footprint. Firebase Crashlytics has been completely removed from the codebase (KID-269 — pending merge), meaning no crash logs or diagnostic data are collected in this build. **Historical note:** prior builds (before KID-269) did ship with Crashlytics enabled (`crashlyticsAvailable = true`), so crash data was collected in earlier versions. No analytics SDKs are bundled with the application. The only retained third-party service is Firebase Cloud Messaging (FCM), which is strictly required to wake the device for incoming call push notifications. Finally, web-based account management and deletion are provided via the Kids.Talk portal at `https://www.kids.talk/account/manage` (KID-279 — portal page pending).

## Section 1: Data Collection and Security

The initial section of the Data Safety form establishes the baseline security and collection practices of the application.

| Questionnaire Item | Declaration | Justification |
|---|---|---|
| Does your app collect or share any of the required user data types? | **Yes** | The app collects SIP credentials and FCM tokens. |
| Is all of the user data collected by your app encrypted in transit? | **Yes** | All SIP signaling is secured via TLS, and all FCM traffic operates over HTTPS. |
| Do you provide a way for users to request that their data be deleted? | **Yes** | Users manage and delete their household accounts via the web portal at `https://www.kids.talk/account/manage`. |

## Section 2: Data Types Inventory

The following table details the specific data types that must be declared as either collected or not collected based on the app's functionality. Note that while the app processes audio for VoIP calls, this data is processed ephemerally in-memory and is neither stored nor recorded, exempting it from collection declaration.

| Category | Data Type | Status | Notes |
|---|---|---|---|
| **Personal Info** | User IDs | **Collected** | The SIP Extension/Username is collected to authenticate and route calls. |
| **Personal Info** | Name, Email, Address, Phone, etc. | Not Collected | No other personal information is requested or processed by the app. |
| **Financial Info** | All types | Not Collected | No payment or financial data is handled. |
| **Location** | All types | Not Collected | No location tracking is implemented. |
| **Web Browsing** | All types | Not Collected | No browsing history is accessed. |
| **App Activity** | All types | Not Collected | No behavioral tracking or analytics are present. |
| **App Info & Performance** | Crash logs, Diagnostics | Not Collected | Crashlytics removed per KID-269 (pending merge). Historical builds did collect crash data. |
| **Device or Other IDs** | Device or other IDs | **Collected** | The FCM Registration Token (Instance ID) is collected. |
| **Audio** | Voice or sound recordings | Not Collected | Audio is processed ephemerally for live VoIP routing only. |

## Section 3: Data Usage and Handling

For each data type marked as collected in Section 2, Google Play requires specific details regarding its handling, sharing, and purpose.

### User IDs (SIP Extension)

The SIP Extension acts as the primary user identifier within the application context.

| Attribute | Declaration |
|---|---|
| **Collection vs. Sharing** | Collected only |
| **Ephemeral Processing** | No |
| **User Choice** | Required for the app to function |
| **Purpose** | App functionality (Authenticating with the PBX and routing calls) |

### Device or Other IDs (FCM Token)

The Firebase Cloud Messaging token is necessary to ensure reliable incoming call delivery when the app is backgrounded.

| Attribute | Declaration |
|---|---|
| **Collection vs. Sharing** | Collected and Shared |
| **Ephemeral Processing** | No |
| **User Choice** | Required for the app to function |
| **Collection Purpose** | App functionality (Waking the device when an incoming call arrives) |
| **Sharing Purpose** | App functionality (Shared with Google's FCM service to deliver the push notification) |

## Section 4: Store Listing Preview

Based on the declarations outlined above, the Google Play Store Data Safety section will present the following summary to users on the app listing page.

**Data shared**
- Device or other IDs (App functionality)

**Data collected**
- Personal info (App functionality)
- Device or other IDs (App functionality)

**Security practices**
- Data is encrypted in transit
- You can request that data be deleted

## Section 5: Account Deletion URL

Per Google Play's Data Safety requirements, the account deletion URL must be declared:

| Field | Value |
|---|---|
| **Account deletion URL** | `https://www.kids.talk/account/manage` |
| **Notes** | Authenticated guardian portal page. Wired to `POST /api/gdpr/erasure-request`. Implemented by KID-279. |

## Finalization Checklist

Before submitting to Google Play Console:

- [ ] KID-269 (Crashlytics removal) merged — verify `crashlyticsAvailable = false` in final build
- [ ] KID-279 (portal account-deletion page) live at `https://www.kids.talk/account/manage`
- [ ] KID-265 (privacy policy) published — verify declarations match this document
- [ ] Final signed APK/AAB built and tested
- [ ] Data Safety form submitted in Play Console by named owner
