# KID-263 — Account Lifecycle and Deletion Evidence Ledger

**Ticket:** KID-263  
**Status:** Red — intentionally incomplete; not a release or compliance conclusion.  
**Android base:** `669d70a9885efb89ddf014be7944319ecce86e17` (`origin/main`, fetched 2026-08-25)  
**Portal dependency base:** `81a0c86c32fa808f9280f5d8d46b08a21f950b90` (`origin/main`, fetched 2026-08-25)

> This ledger is the evidence record for determining whether the exact candidate creates an app account under Google Play policy. It does not itself prove functional deletion, reviewer access, the exact signed candidate, encryption in transit, FCM wake, or background calling.

## Red acceptance criteria

The focused validator must fail until every lifecycle flow below identifies its data, owner, system of record, user controls, deletion posture, and evidence source. It must also reject an unsupported final conclusion.

## Lifecycle evidence matrix

| ID | Flow / data | Lifecycle owner | System of record / storage | User create, change, delete controls | Deletion posture | Evidence / source | Current status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| L1 | Handset first-run numeric extension and SIP password entry | **TBD** | **TBD** | **TBD** | **TBD** | **TBD** | Needs evidence |
| L2 | Linphone SIP registration/configuration and possible local credential persistence | **TBD** | **TBD** | **TBD** | **TBD** | **TBD** | Needs evidence |
| L3 | Portal activation family, endpoint, and encrypted handset SIP-secret records | **TBD** | **TBD** | **TBD** | **TBD** | **TBD** | Needs evidence |
| L4 | Authenticated `portal_accounts` record | **TBD** | **TBD** | **TBD** | **TBD** | **TBD** | Needs evidence |
| L5 | Account-management web page at `https://www.kids.talk/account/manage` | **TBD** | **TBD** | **TBD** | **TBD** | **TBD** | Needs evidence |
| L6 | `POST /api/gdpr/erasure-request` server-side erasure path | **TBD** | **TBD** | **TBD** | **TBD** | **TBD** | Needs evidence |
| L7 | Exact signed Android candidate and observed device/Play-track behavior | **TBD** | **TBD** | **TBD** | **TBD** | **TBD** | Needs evidence |

## Shared-fact reconciliation

| Consumer / dependency | KID-263 deliverable required before consumption | Current status |
| --- | --- | --- |
| KID-264 — Data Safety | Approved lifecycle and deletion-path fact only | **TBD** |
| KID-265 — Privacy policy | Approved lifecycle/deletion wording and canonical URL only | **TBD** |
| KID-414 — Reviewer access/declarations | Reusable reviewer test preconditions only | **TBD** |
| KID-413 — Signed-candidate QA | Exact candidate identity and functional-path prerequisite | **TBD** |
| KID-279 — portal deletion page | Canonical authenticated page and end-to-end erasure evidence | **TBD** |

## Decision and acceptance state

**Lifecycle conclusion:** PENDING — do not classify the product as creating or not creating an app account from this incomplete ledger.  
**Functional deletion conclusion:** PENDING — a deep link is not evidence of a working deletion flow.  
**Exact candidate:** PENDING — no signed AAB identity, signer/provenance, installation identity, or observed behavior has been supplied.  
**Release acceptance:** BLOCKED.

## Required evidence still missing

The Green ledger must resolve each matrix field from current-source or exact-candidate evidence, use `Unknown — evidence not available` only with a named owner and an explicit external gate, and preserve the open blockers for KID-267, KID-279, KID-264, KID-265, KID-414, and KID-413. It must not guess retention, local credential persistence, portal-account creation ownership, or live deletion behavior.

## Redaction

Do not add passwords, SIP credentials, signing material, Firebase configuration values, tokens, reviewer/customer credentials, raw device identifiers, or raw sensitive logs to this ledger.
