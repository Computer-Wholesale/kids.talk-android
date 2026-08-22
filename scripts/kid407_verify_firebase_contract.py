#!/usr/bin/env python3
"""Fail-closed, redacted Firebase Android client-binding contract verifier for KID-407."""

from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path
from typing import Any

EXPECTED_PROJECT_ID = "kids-talk-2fbbe"
EXPECTED_PACKAGE_NAME = "com.kidstalk.phone"
EXPECTED_SENDER_ID = "506199291713"
TRACKED_CONFIGURATION_PATH = "app/google-services.json"


def fail(message: str) -> None:
    print(f"KID407_CONTRACT_FAILURE={message}", file=sys.stderr)
    raise SystemExit(1)


def git_succeeds(repository_root: Path, *arguments: str) -> bool:
    completed = subprocess.run(
        ["git", *arguments],
        cwd=repository_root,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        check=False,
        text=True,
    )
    return completed.returncode == 0


def load_configuration(configuration_path: Path) -> dict[str, Any]:
    if not configuration_path.is_file():
        fail("SUPPLIED_CONFIGURATION_MISSING")
    try:
        parsed = json.loads(configuration_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        fail("SUPPLIED_CONFIGURATION_INVALID")
    if not isinstance(parsed, dict):
        fail("SUPPLIED_CONFIGURATION_INVALID")
    return parsed


def client_package_name(client: Any) -> str | None:
    if not isinstance(client, dict):
        return None
    client_info = client.get("client_info")
    if not isinstance(client_info, dict):
        return None
    android_client_info = client_info.get("android_client_info")
    if not isinstance(android_client_info, dict):
        return None
    package_name = android_client_info.get("package_name")
    return package_name if isinstance(package_name, str) else None


def verify_no_fcm(repository_root: Path, configuration_path: Path) -> None:
    if configuration_path.exists():
        fail("NO_FCM_CONFIGURATION_PRESENT")
    if git_succeeds(repository_root, "ls-files", "--error-unmatch", TRACKED_CONFIGURATION_PATH):
        fail("UPSTREAM_CONFIGURATION_STILL_TRACKED")
    if not git_succeeds(repository_root, "check-ignore", "-q", TRACKED_CONFIGURATION_PATH):
        fail("PRODUCTION_CONFIGURATION_NOT_IGNORED")

    print("CONFIG_MODE=NO_FCM")
    print("CONFIGURATION_FILE_PRESENT=false")
    print("TRACKED_CONFIGURATION=false")
    print("IGNORED_CONFIGURATION=true")
    print("UPSTREAM_FALLBACK=false")
    print("API_KEY_VALUE_REDACTED=true")


def verify_supplied(configuration_path: Path) -> None:
    configuration = load_configuration(configuration_path)
    project_info = configuration.get("project_info")
    if not isinstance(project_info, dict):
        fail("PROJECT_INFO_MISSING")

    project_id = project_info.get("project_id")
    sender_id = project_info.get("project_number")
    if project_id != EXPECTED_PROJECT_ID:
        fail("PROJECT_ID_MISMATCH")
    if sender_id != EXPECTED_SENDER_ID:
        fail("SENDER_ID_MISMATCH")

    clients = configuration.get("client")
    if not isinstance(clients, list):
        fail("CLIENT_LIST_MISSING")
    matching_clients = [client for client in clients if client_package_name(client) == EXPECTED_PACKAGE_NAME]
    if len(matching_clients) != 1:
        fail("ANDROID_CLIENT_MATCH_COUNT_INVALID")

    api_key_values_present = False
    for client in matching_clients:
        api_keys = client.get("api_key") if isinstance(client, dict) else None
        if isinstance(api_keys, list):
            api_key_values_present = any(
                isinstance(api_key, dict) and isinstance(api_key.get("current_key"), str) and bool(api_key["current_key"])
                for api_key in api_keys
            )
    if not api_key_values_present:
        fail("ANDROID_CLIENT_API_KEY_MISSING")

    print("CONFIG_MODE=SUPPLIED")
    print(f"FIREBASE_PROJECT_ID={EXPECTED_PROJECT_ID}")
    print(f"ANDROID_PACKAGE_NAME={EXPECTED_PACKAGE_NAME}")
    print(f"FCM_SENDER_ID={EXPECTED_SENDER_ID}")
    print(f"CLIENT_MATCH_COUNT={len(matching_clients)}")
    print("API_KEY_VALUE_REDACTED=true")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repository-root", required=True, type=Path)
    parser.add_argument("--config", required=True, type=Path)
    parser.add_argument("--mode", required=True, choices=("no-fcm", "supplied"))
    arguments = parser.parse_args()

    repository_root = arguments.repository_root.resolve()
    if not (repository_root / ".git").exists():
        fail("REPOSITORY_ROOT_INVALID")

    if arguments.mode == "no-fcm":
        verify_no_fcm(repository_root, arguments.config)
    else:
        verify_supplied(arguments.config)


if __name__ == "__main__":
    main()
