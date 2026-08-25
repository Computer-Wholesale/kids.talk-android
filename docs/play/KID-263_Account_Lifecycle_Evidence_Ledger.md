# KID-263 — Account Lifecycle and Deletion Evidence Ledger

**Ticket:** KID-263
**Status:** Green for the current-source evidence record; **not** a release-acceptance or functional-deletion result.
**Android base:** `669d70a9885efb89ddf014be7944319ecce86e17` (`origin/main`, fetched 2026-08-25)
**Portal dependency base:** `81a0c86c32fa808f9280f5d8d46b08a21f950b90` (`origin/main`, fetched 2026-08-25)

> The exact final signed candidate and observed device/Play-track behavior outrank this source ledger. This document deliberately records unavailable evidence as an external gate rather than inferring a compliance conclusion.

## Evidence references

| Ref. | Immutable source / record | What it supports and does not support |
| --- | --- | --- |
| S1 | Android `origin/main` `app/src/main/assets/linphonerc_factory`, lines 80–86 | The current source caps accounts at one and hides account creation, third-party account creation, advanced settings, and settings. It does **not** prove the identity lifecycle or candidate behavior. |
| S2 | Android `origin/main` `app/src/main/java/org/linphone/ui/setup/KidsTalkSetupActivity.kt`, lines 152–170 and 303–316 | The setup surface accepts a numeric extension and password and calls `viewModel.registerAccount`. This file does **not** show the ViewModel implementation, credential issuer, persistence location, or deletion behavior. |
| S3 | Portal `origin/main` `api/src/routes/numberBundleActivate.ts`, lines 459–506 | The activation path creates `families` and `endpoints` records and encrypts the handset SIP secret before insertion. It does **not** create `portal_accounts` in this route or establish exact-candidate linkage. |
| S4 | Portal `origin/main` `api/src/routes/auth.ts` and current-source scan | `portal_accounts` are used for authenticated login. The scanned current API source contains no `INSERT INTO portal_accounts`; ownership/creation flow remains an external gate. |
| S5 | Portal `origin/main` `api/src/routes/gdpr.ts`, lines 72–112 | An authenticated erasure endpoint soft-deletes `portal_accounts` and `families` and deletes matching `activity_log` entries. This is source evidence only, not live end-to-end deletion proof or a complete retention analysis. |
| S6 | Linear KID-279 current ticket | KID-279 owns the authenticated canonical `https://www.kids.talk/account/manage` page and end-to-end erasure evidence. It remains a blocking external dependency for KID-263. |
| S7 | Android GitHub prerelease `v1.14` | The prerelease targets `main` and contains no release assets. No signed AAB identity, SHA-256, signer/provenance, versionCode, device install result, or Play-delivered result is available. |
| S8 | Linear KID-263 architect review, 2026-08-25 | PRs #6 and #7 are stale/conflicting historical work with no checks; they are not current-base or candidate evidence. The architect requires a Red/Green ledger, current-base validation, candidate identity when available, KID-279 evidence, and reconciliation before acceptance. |

## Lifecycle evidence matrix

| ID | Flow / data | Lifecycle owner | System of record / storage | User create, change, delete controls | Deletion posture | Evidence / source | Current status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| L1 | Handset first-run numeric extension and SIP password entry | Credential issuer is **not established** by Android source; the Guardian enters values into the handset setup UI. | The visible Activity holds the entered values during the setup interaction; persistence after registration is **not established** by S2. | The Guardian can enter or replace the two input values before registration. S1 hides in-app account-creation flows; S2 does not expose deletion. | This registration input is not sufficient evidence of an app-account lifecycle or a deletion path. | S1, S2 | Evidence bounded; issuer and persistence are external gates. |
| L2 | Linphone SIP registration/configuration and possible local credential persistence | Linphone core registration implementation; local persistence owner and path are **not established** in the inspected current source. | **Unknown — external gate:** S2 delegates to a missing/uninspected ViewModel/core path. No credential value is recorded in this ledger. | S1 hides account-management/settings surfaces. The inspected source shows no supported local deletion control. | No local wipe is proposed or evidenced; it must not substitute for authenticated server-side deletion. | S1, S2 | Evidence bounded; local persistence/deletion requires separate implementation evidence. |
| L3 | Portal activation family, endpoint, and encrypted handset SIP-secret records | Portal activation service. | Portal relational `families` and `endpoints` records; handset secret is encrypted before endpoint insertion. | S3 proves server-side record creation in the activation route, but not the precise user journey, edit controls, or linkage to an exact Android candidate. | KID-279/KID-263 evidence is required before asserting that related service records are functionally deletable through a reachable user path. | S3, S5, S6 | Evidence bounded; end-to-end user control remains an external gate. |
| L4 | Authenticated `portal_accounts` record | Portal authentication lifecycle owner is **not established** in the inspected current API source. | Portal `portal_accounts` table is queried for authentication and updated by the erasure endpoint. | Login is supported by current source; creation, amendment, and independent deletion controls are **unknown — external gate** because no creation route was found in the scanned API source. | S5 provides source-level soft-delete behavior only; it does not establish user reachability or all retained records. | S4, S5, S6 | Evidence bounded; account-creation owner and live proof are external gates. |
| L5 | Account-management web page at `https://www.kids.talk/account/manage` | KID-279. | The canonical page is not present in the current portal source scan; KID-279 defines it as the user-facing deletion entry point. | Authentication, confirmation, error/retry, and successful user deletion must be supplied by KID-279. | **Not proven.** A deep link, historical PR, or endpoint source code is not functional deletion evidence. | S6, S8 | Blocked by KID-279; no live-site access or production deletion was attempted. |
| L6 | `POST /api/gdpr/erasure-request` server-side erasure path | Portal GDPR route and authenticated backend. | S5 updates `portal_accounts` and `families` with `deleted_at` and deletes matching `activity_log` records. Retention of other legal, billing, support, or audit records is not established. | An authenticated request is required by source. User-facing confirmation, error/retry, and canonical-page reachability are KID-279 evidence gates. | **Not proven end-to-end.** Source is consistent with a server-side erasure action but not a validated, reachable deletion path. | S5, S6 | Blocked by KID-279 and exact-candidate/reviewer validation. |
| L7 | Exact signed Android candidate and observed device/Play-track behavior | KID-267 supplies signing/provenance; KID-413 supplies independent exact-candidate and Play-delivered QA. | **External gate — no candidate supplied.** GitHub prerelease `v1.14` has no release assets. | Candidate-specific UI/deep-link behavior cannot be claimed without a signed artifact, installed identity, and observed result. | **Not proven.** Candidate evidence must include AAB SHA-256, signer/provenance, versionName/versionCode, install identity, and tested behavior. | S7, S8 | Blocked by KID-267 and KID-413. |

## Shared-fact reconciliation

| Consumer / dependency | KID-263 evidence supplied or required | Current status |
| --- | --- | --- |
| KID-264 — Data Safety | Consume only the bounded lifecycle facts above: current source does not establish the portal-account creation flow, local credential persistence, functional deletion, or exact candidate. | Await KID-264 mapping reconciliation; no mapping was edited. |
| KID-265 — Privacy policy | Consume only approved lifecycle/deletion wording and KID-279’s canonical URL after its implementation evidence exists. | Await KID-265 reconciliation; no policy prose was edited. |
| KID-414 — Reviewer access/declarations | Reviewer instructions may state the required canonical authenticated path only after KID-279 makes it reachable; no reviewer-access claim is made. | Await KID-414 reconciliation. |
| KID-413 — Signed-candidate QA | Requires signed AAB identity and KID-279’s end-to-end erasure evidence before independent candidate/Play-track validation. | Await KID-267, KID-279, and KID-413 gates. |
| KID-279 — portal deletion page | Owns the authenticated canonical page, user-facing confirmation/error/retry, and end-to-end erasure result. | Blocking dependency; no portal change or live deletion was attempted. |
| KID-267 — signed release/AAB | Owns protected signing/provenance and candidate identity. | Blocking evidence gate; no signing material was accessed. |
| KID-270 / historical Android PR #7 | Owns the About-sheet deletion-link surface in historical work. | PR #7 is stale/conflicting and not used as completion evidence. |

## Decision and acceptance state

**Lifecycle conclusion:** INDETERMINATE FOR PLAY-POLICY CLASSIFICATION — current source shows restricted SIP registration and portal service records, but does not establish the complete app-account creation lifecycle for the exact candidate.
**Functional deletion conclusion:** NOT PROVEN — no authenticated canonical deletion page, live end-to-end erasure result, or exact-candidate interaction evidence is available.
**Exact candidate:** EXTERNAL GATE — KID-267 must provide signed AAB identity, provenance, and installation evidence; KID-413 must independently validate it.
**Release acceptance:** BLOCKED — KID-279, KID-267, KID-413, and reconciliation with KID-264, KID-265, and KID-414 remain required.

## Required follow-on evidence

KID-279 must provide the authenticated canonical page, confirmation/error/retry behavior, and a redacted end-to-end erasure result. KID-267 must provide the exact signed candidate’s provenance. KID-413 must validate the exact installed candidate and Play-delivered build. KID-264, KID-265, and KID-414 must consume this bounded conclusion without asserting facts beyond their assigned evidence.

## Redaction

This ledger contains no passwords, SIP credentials, signing material, Firebase configuration values, tokens, reviewer/customer credentials, raw device identifiers, raw sensitive logs, or production-deletion results.
