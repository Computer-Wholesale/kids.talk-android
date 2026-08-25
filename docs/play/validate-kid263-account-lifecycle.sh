#!/usr/bin/env bash
# KID-263 focused evidence validator. It validates the ledger's completeness
# and claim boundaries; it does not execute live deletion or candidate tests.
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
require '## Evidence references' 'evidence-reference section'
require '## Lifecycle evidence matrix' 'lifecycle evidence matrix'
require '## Shared-fact reconciliation' 'shared-fact reconciliation section'
require '## Decision and acceptance state' 'decision and acceptance state'
require '## Redaction' 'redaction section'

for flow in L1 L2 L3 L4 L5 L6 L7; do
  if ! grep -Eq "^\| ${flow} \|" "$ledger"; then
    fail "missing ${flow} lifecycle flow"
  fi
done

for ticket in KID-264 KID-265 KID-267 KID-279 KID-414 KID-413; do
  require "$ticket" "${ticket} reconciliation entry"
done

if grep -Fq '**TBD**' "$ledger"; then
  fail 'unresolved TBD field(s) remain in lifecycle matrix or reconciliation table'
fi

if grep -Eq '\*\*(Lifecycle conclusion|Functional deletion conclusion|Exact candidate|Release acceptance):\*\* PENDING' "$ledger"; then
  fail 'a required conclusion remains PENDING'
fi

require 'INDETERMINATE FOR PLAY-POLICY CLASSIFICATION' 'bounded lifecycle conclusion'
require 'Functional deletion conclusion:** NOT PROVEN' 'non-functional deletion conclusion'
require 'Exact candidate:** EXTERNAL GATE' 'exact-candidate external gate'
require 'Release acceptance:** BLOCKED' 'release-acceptance blocker'
require 'No local wipe is proposed or evidenced' 'local-wipe prohibition'
require 'no live-site access or production deletion was attempted' 'live-operation boundary'

if grep -Fqi 'functional deletion conclusion:** proven' "$ledger"; then
  fail 'unsupported functional-deletion conclusion'
fi

if (( failures > 0 )); then
  printf 'KID-263 lifecycle evidence validation failed with %d issue(s).\n' "$failures" >&2
  exit 1
fi

printf 'PASS: KID-263 lifecycle evidence ledger is complete and claim-bounded.\n'
