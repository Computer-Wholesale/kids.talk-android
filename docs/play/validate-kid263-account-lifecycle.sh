#!/usr/bin/env bash
# KID-263 focused evidence validator. It validates the ledger's completeness,
# not live deletion behavior or a signed-candidate claim.
set -euo pipefail

ledger="${1:-docs/play/KID-263_Account_Lifecycle_Evidence_Ledger.md}"

if [[ ! -f "$ledger" ]]; then
  printf 'FAIL: ledger not found: %s\n' "$ledger" >&2
  exit 1
fi

failures=0
fail() {
  printf 'FAIL: %s\n' "$1" >&2
  failures=$((failures + 1))
}

require() {
  local text="$1"
  local description="$2"
  if ! grep -Fq -- "$text" "$ledger"; then
    fail "missing ${description}"
  fi
}

require 'Android base:' 'Android base identity'
require 'Portal dependency base:' 'portal dependency base identity'
require '## Lifecycle evidence matrix' 'lifecycle evidence matrix'
require '## Shared-fact reconciliation' 'shared-fact reconciliation section'
require '## Decision and acceptance state' 'decision and acceptance state'
require '## Redaction' 'redaction section'

for flow in L1 L2 L3 L4 L5 L6 L7; do
  if ! grep -Eq "^\| ${flow} \|" "$ledger"; then
    fail "missing ${flow} lifecycle flow"
  fi
done

for ticket in KID-264 KID-265 KID-279 KID-414 KID-413; do
  require "$ticket" "${ticket} reconciliation entry"
done

if grep -Fq '**TBD**' "$ledger"; then
  fail 'unresolved TBD field(s) remain in lifecycle matrix or reconciliation table'
fi

if grep -Eq '\*\*(Lifecycle conclusion|Functional deletion conclusion|Exact candidate|Release acceptance):\*\* PENDING' "$ledger"; then
  fail 'a required conclusion remains PENDING'
fi

if grep -Fqi 'functional deletion.*proven' "$ledger" && ! grep -Fq 'exact signed candidate' "$ledger"; then
  fail 'functional-deletion claim is not tied to exact-candidate evidence'
fi

if grep -Fqi 'background' "$ledger" || grep -Fqi 'FCM wake' "$ledger" || grep -Fqi 'encryption in transit' "$ledger"; then
  fail 'ledger contains an out-of-scope unsupported transport, wake, or background claim'
fi

if (( failures > 0 )); then
  printf 'KID-263 lifecycle evidence validation failed with %d issue(s).\n' "$failures" >&2
  exit 1
fi

printf 'PASS: KID-263 lifecycle evidence ledger is complete and claim-bounded.\n'
