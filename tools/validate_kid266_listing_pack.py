#!/usr/bin/env python3
"""Validate KID-266 public-listing claims and asset records.

The validator deliberately fails closed. A listing pack is not complete unless every
public statement is linked to exact-candidate evidence and its designated evidence
owner, every planned asset has provenance and Play-compatible metadata, and the
submission draft avoids prohibited unsupported capability claims.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any

REPOSITORY_ROOT = Path(__file__).resolve().parents[1]
PACK_DIRECTORY = REPOSITORY_ROOT / "docs" / "play" / "store_assets"
LEDGER_PATH = PACK_DIRECTORY / "KID-266_listing_claim_ledger.md"
MANIFEST_PATH = PACK_DIRECTORY / "KID-266_asset_manifest.json"
DRAFT_PATH = PACK_DIRECTORY / "KID-266_listing_submission_draft.md"

PROHIBITED_UNSUPPORTED_TERMS = (
    "secure",
    "security",
    "tls",
    "srtp",
    "encrypted",
    "encryption",
    "fcm",
    "background calling",
    "locked-device",
    "lock-screen",
    "full-screen incoming",
    "no analytics",
    "data minimisation",
    "data minimization",
    "child-safe",
    "parental control",
)

REQUIRED_OWNERS = {
    "KID-263",
    "KID-264",
    "KID-265",
    "KID-267",
    "KID-269",
    "KID-413",
    "KID-414",
}


def load_manifest(path: Path) -> dict[str, Any]:
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        raise ValueError(f"missing manifest: {path.relative_to(REPOSITORY_ROOT)}")
    except json.JSONDecodeError as error:
        raise ValueError(f"manifest is not valid JSON: {error}") from error
    if not isinstance(data, dict):
        raise ValueError("manifest root must be a JSON object")
    return data


def markdown_text(path: Path, label: str, errors: list[str]) -> str:
    if not path.is_file():
        errors.append(f"missing {label}: {path.relative_to(REPOSITORY_ROOT)}")
        return ""
    return path.read_text(encoding="utf-8")


def validate_candidate(manifest: dict[str, Any], errors: list[str]) -> None:
    candidate = manifest.get("candidate_evidence")
    if not isinstance(candidate, dict):
        errors.append("candidate_evidence must be an object")
        return
    required_fields = ("aab_sha256", "version_name", "version_code", "evidence_ref")
    for field in required_fields:
        value = candidate.get(field)
        if not isinstance(value, str) or not value.strip() or value == "PENDING":
            errors.append(f"candidate_evidence.{field} is unresolved")
    if candidate.get("status") != "verified":
        errors.append("candidate_evidence.status must be 'verified'")


def validate_claims(manifest: dict[str, Any], errors: list[str]) -> None:
    claims = manifest.get("claims")
    if not isinstance(claims, list) or not claims:
        errors.append("claims must contain every public listing statement")
        return

    seen_ids: set[str] = set()
    owners_seen: set[str] = set()
    for index, claim in enumerate(claims, start=1):
        prefix = f"claims[{index}]"
        if not isinstance(claim, dict):
            errors.append(f"{prefix} must be an object")
            continue
        claim_id = claim.get("id")
        statement = claim.get("statement")
        owner = claim.get("owner")
        evidence_refs = claim.get("evidence_refs")
        if not isinstance(claim_id, str) or not claim_id.strip():
            errors.append(f"{prefix}.id is required")
        elif claim_id in seen_ids:
            errors.append(f"duplicate claim id: {claim_id}")
        else:
            seen_ids.add(claim_id)
        if not isinstance(statement, str) or not statement.strip():
            errors.append(f"{prefix}.statement is required")
        if owner not in REQUIRED_OWNERS and owner != "KID-266":
            errors.append(f"{prefix}.owner must name KID-266 or an approved shared-fact owner")
        elif isinstance(owner, str):
            owners_seen.add(owner)
        if not isinstance(evidence_refs, list) or not evidence_refs or not all(
            isinstance(reference, str) and reference.strip() and reference != "PENDING"
            for reference in evidence_refs
        ):
            errors.append(f"{prefix}.evidence_refs must contain non-pending evidence")
        if claim.get("status") != "supported":
            errors.append(f"{prefix}.status must be 'supported'")

    missing_owners = REQUIRED_OWNERS - owners_seen
    if missing_owners:
        errors.append(
            "claim ledger does not reconcile the required shared-fact owners: "
            + ", ".join(sorted(missing_owners))
        )


def validate_assets(manifest: dict[str, Any], errors: list[str]) -> None:
    assets = manifest.get("assets")
    if not isinstance(assets, list) or not assets:
        errors.append("assets must inventory all public listing assets")
        return

    seen_paths: set[str] = set()
    for index, asset in enumerate(assets, start=1):
        prefix = f"assets[{index}]"
        if not isinstance(asset, dict):
            errors.append(f"{prefix} must be an object")
            continue
        path_value = asset.get("path")
        asset_type = asset.get("type")
        provenance = asset.get("provenance")
        evidence_ref = asset.get("evidence_ref")
        if not isinstance(path_value, str) or not path_value.strip():
            errors.append(f"{prefix}.path is required")
            continue
        if path_value in seen_paths:
            errors.append(f"duplicate asset path: {path_value}")
        seen_paths.add(path_value)
        if asset_type not in {"icon", "feature_graphic", "screenshot"}:
            errors.append(f"{prefix}.type is invalid")
        for field, value in (("provenance", provenance), ("evidence_ref", evidence_ref)):
            if not isinstance(value, str) or not value.strip() or value == "PENDING":
                errors.append(f"{prefix}.{field} is unresolved")
        width = asset.get("width_px")
        height = asset.get("height_px")
        if not isinstance(width, int) or width <= 0 or not isinstance(height, int) or height <= 0:
            errors.append(f"{prefix} has invalid dimensions")
        if asset_type == "icon" and (width, height) != (512, 512):
            errors.append(f"{prefix} icon must be 512x512")
        if asset_type == "feature_graphic" and (width, height) != (1024, 500):
            errors.append(f"{prefix} feature graphic must be 1024x500")
        if asset_type == "screenshot" and not (320 <= width <= 3840 and 320 <= height <= 3840):
            errors.append(f"{prefix} screenshot sides must each be 320–3840px")


def validate_public_text(text: str, errors: list[str]) -> None:
    lower_text = text.lower()
    for term in PROHIBITED_UNSUPPORTED_TERMS:
        if term in lower_text:
            errors.append(f"submission draft contains unsupported claim term: {term!r}")
    short_match = re.search(r"^Short description:\s*(.+)$", text, re.MULTILINE)
    if not short_match:
        errors.append("submission draft must include a 'Short description:' line")
    elif len(short_match.group(1).strip()) > 80:
        errors.append("short description exceeds 80 characters")
    full_start = re.search(r"^Full description:\s*$", text, re.MULTILINE)
    if not full_start:
        errors.append("submission draft must include a 'Full description:' heading")
    else:
        full_description = text[full_start.end() :].strip()
        if len(full_description) > 4000:
            errors.append("full description exceeds 4000 characters")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--expect-incomplete",
        action="store_true",
        help="succeed only when the pack correctly fails complete-mode validation",
    )
    arguments = parser.parse_args()

    errors: list[str] = []
    ledger_text = markdown_text(LEDGER_PATH, "claim ledger", errors)
    submission_text = markdown_text(DRAFT_PATH, "submission draft", errors)
    if "Status: COMPLETE" not in ledger_text:
        errors.append("claim ledger must be explicitly marked 'Status: COMPLETE'")

    try:
        manifest = load_manifest(MANIFEST_PATH)
    except ValueError as error:
        errors.append(str(error))
        manifest = {}

    validate_candidate(manifest, errors)
    validate_claims(manifest, errors)
    validate_assets(manifest, errors)
    validate_public_text(submission_text, errors)

    if arguments.expect_incomplete:
        if errors:
            print("Expected incomplete pack correctly failed complete-mode validation:")
            for error in errors:
                print(f"- {error}")
            return 0
        print("Expected incomplete pack unexpectedly passed complete-mode validation", file=sys.stderr)
        return 1

    if errors:
        print("KID-266 listing pack validation failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1

    print("KID-266 listing pack validation passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
