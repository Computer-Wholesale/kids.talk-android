# KID-266 — Public Listing Claim Ledger

**Status: INCOMPLETE**

> This Red-stage ledger deliberately contains no supported public capability, privacy, security, target-audience, IARC, ad, reviewer-access, or candidate-behavior claim. A listing pack becomes eligible for completion only when every statement and asset is tied to the exact signed candidate and to its owner evidence.

| ID | Public statement or asset claim | Evidence owner | Exact-candidate evidence | Status |
| --- | --- | --- | --- | --- |
| CAND-001 | Candidate identity: AAB SHA-256, version name, versionCode and provenance | KID-267 | PENDING | Incomplete |
| CALL-001 | Reached/in-app calling posture, including any screenshot-visible behavior | KID-413 | PENDING | Incomplete |
| DATA-001 | Data collection, sharing, analytics/Crashlytics, advertising and Data Safety alignment | KID-264 and KID-269 | PENDING | Incomplete |
| POLICY-001 | Privacy-policy URL, support contact and any deletion/policy reference | KID-263 and KID-265 | PENDING | Incomplete |
| DECL-001 | Target audience, Families analysis, IARC inputs, ads declaration and reviewer-access consistency | KID-414 | PENDING | Incomplete |
| ASSET-001 | Icon, feature graphic and final-candidate screenshot provenance | KID-266 with KID-413 evidence | PENDING | Incomplete |

## Explicitly excluded until supported

The public listing draft must not state or imply TLS/SRTP, encryption in transit, FCM wake, dependable background/suspended/locked-device calling, full-screen incoming calling, deletion availability, reviewer access, Play approval/readiness, security properties, data minimisation, no analytics, child safety, parental controls, or ad status unless the final evidence record supports the precise statement.

## Validation

Run from the Android repository root:

```sh
python3 tools/validate_kid266_listing_pack.py
```

The Red baseline must fail in complete mode. Its expected-failure assertion is:

```sh
python3 tools/validate_kid266_listing_pack.py --expect-incomplete
```
