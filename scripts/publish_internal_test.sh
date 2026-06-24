#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CONFIG_PATH="${INTERVALSGYM_PUBLISH_CONFIG:-/Users/hyunwoo.pr/Dev/private_settings/intervalsgym_publish_config.json}"

if [[ ! -f "$CONFIG_PATH" ]]; then
  echo "Publish config not found: $CONFIG_PATH" >&2
  exit 1
fi

read_json() {
  local key="$1"
  python3 - "$CONFIG_PATH" "$key" <<'PY'
import json
import sys
path, key = sys.argv[1], sys.argv[2]
with open(path, encoding="utf-8") as f:
    data = json.load(f)
value = data.get(key, "")
print(value if value is not None else "")
PY
}

SIGNING_STORE_FILE="$(read_json signingStoreFile)"
SIGNING_STORE_PASSWORD="$(read_json signingStorePassword)"
SIGNING_KEY_ALIAS="$(read_json signingKeyAlias)"
SIGNING_KEY_PASSWORD="$(read_json signingKeyPassword)"
PLAY_SERVICE_ACCOUNT_JSON="$(read_json playServiceAccountJson)"
BUILD_GRADLE_PATH="$ROOT_DIR/app/build.gradle.kts"
RELEASE_NOTES_LANGUAGE="${INTERVALSGYM_RELEASE_NOTES_LANGUAGE:-ko-KR}"
RELEASE_NOTES_TRACK="${INTERVALSGYM_RELEASE_NOTES_TRACK:-internal}"
RELEASE_NOTES_PATH="$ROOT_DIR/app/src/main/play/release-notes/$RELEASE_NOTES_LANGUAGE/$RELEASE_NOTES_TRACK.txt"

missing=()
[[ -n "$SIGNING_STORE_FILE" && "$SIGNING_STORE_FILE" != TODO* ]] || missing+=(signingStoreFile)
[[ -n "$SIGNING_STORE_PASSWORD" && "$SIGNING_STORE_PASSWORD" != TODO* ]] || missing+=(signingStorePassword)
[[ -n "$SIGNING_KEY_ALIAS" && "$SIGNING_KEY_ALIAS" != TODO* ]] || missing+=(signingKeyAlias)
[[ -n "$SIGNING_KEY_PASSWORD" && "$SIGNING_KEY_PASSWORD" != TODO* ]] || missing+=(signingKeyPassword)
[[ -n "$PLAY_SERVICE_ACCOUNT_JSON" && "$PLAY_SERVICE_ACCOUNT_JSON" != TODO* ]] || missing+=(playServiceAccountJson)

if (( ${#missing[@]} > 0 )); then
  printf 'Publish config has missing placeholder values: %s\n' "${missing[*]}" >&2
  printf 'Edit: %s\n' "$CONFIG_PATH" >&2
  exit 1
fi

if [[ ! -f "$SIGNING_STORE_FILE" ]]; then
  echo "Signing keystore not found: $SIGNING_STORE_FILE" >&2
  exit 1
fi

if [[ ! -f "$PLAY_SERVICE_ACCOUNT_JSON" ]]; then
  echo "Play service account JSON not found: $PLAY_SERVICE_ACCOUNT_JSON" >&2
  exit 1
fi

if python3 - "$PLAY_SERVICE_ACCOUNT_JSON" <<'PY'
import json
import sys

path = sys.argv[1]
try:
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
except Exception:
    raise SystemExit(1)

required = ["type", "project_id", "private_key_id", "private_key", "client_email", "client_id", "token_uri"]
missing = [key for key in required if not data.get(key) or str(data.get(key)).startswith("TODO")]
raise SystemExit(1 if missing else 0)
PY
then
  :
else
  echo "Play service account JSON has missing TODO values: $PLAY_SERVICE_ACCOUNT_JSON" >&2
  exit 1
fi

bump_version() {
  python3 - "$BUILD_GRADLE_PATH" <<'PY'
import re
import sys
from pathlib import Path

path = Path(sys.argv[1])
text = path.read_text(encoding="utf-8")

code_match = re.search(r"versionCode\s*=\s*(\d+)", text)
name_match = re.search(r'versionName\s*=\s*"(\d+(?:\.\d+)*)"', text)

if not code_match:
    raise SystemExit(f"versionCode not found in {path}")
if not name_match:
    raise SystemExit(f"numeric versionName not found in {path}")

old_code = int(code_match.group(1))
old_name = name_match.group(1)
parts = old_name.split(".")
while len(parts) < 3:
    parts.append("0")
parts[-1] = str(int(parts[-1]) + 1)
new_code = old_code + 1
new_name = ".".join(parts)

text = text[:code_match.start(1)] + str(new_code) + text[code_match.end(1):]
name_match = re.search(r'versionName\s*=\s*"(\d+(?:\.\d+)*)"', text)
text = text[:name_match.start(1)] + new_name + text[name_match.end(1):]

path.write_text(text, encoding="utf-8")
print(f"Bumped versionCode {old_code} -> {new_code}")
print(f"Bumped versionName {old_name} -> {new_name}")
print(f"new_version={new_name}")
PY
}

current_version_name() {
  python3 - "$BUILD_GRADLE_PATH" <<'PY'
import re
import sys
from pathlib import Path

text = Path(sys.argv[1]).read_text(encoding="utf-8")
match = re.search(r'versionName\s*=\s*"(\d+(?:\.\d+)*)"', text)
if not match:
    raise SystemExit(f"numeric versionName not found in {sys.argv[1]}")
print(match.group(1))
PY
}

ensure_release_targets_clean() {
  if [[ "${INTERVALSGYM_SKIP_RELEASE_COMMIT:-0}" == "1" ]]; then
    return
  fi

  if ! git diff --quiet -- "$BUILD_GRADLE_PATH" ||
     ! git diff --cached --quiet -- "$BUILD_GRADLE_PATH" ||
     { [[ -e "$RELEASE_NOTES_PATH" ]] && ! git diff --quiet -- "$RELEASE_NOTES_PATH"; } ||
     { [[ -e "$RELEASE_NOTES_PATH" ]] && ! git diff --cached --quiet -- "$RELEASE_NOTES_PATH"; } ||
     { [[ -e "$RELEASE_NOTES_PATH" ]] && [[ -z "$(git ls-files -- "$RELEASE_NOTES_PATH")" ]]; }; then
    echo "Release commit target files are already dirty." >&2
    echo "Commit or stash these first, or set INTERVALSGYM_SKIP_RELEASE_COMMIT=1:" >&2
    echo "  $BUILD_GRADLE_PATH" >&2
    echo "  $RELEASE_NOTES_PATH" >&2
    exit 1
  fi
}

generate_release_notes() {
  local last_release_commit
  local subjects

  last_release_commit="$(git log --grep='^chore: release ' --format='%H' -n 1 || true)"
  if [[ -n "$last_release_commit" ]]; then
    subjects="$(git log --reverse --no-merges --format='%s' "${last_release_commit}..HEAD")"
  else
    subjects="$(git log --reverse --no-merges --format='%s' -n 20)"
  fi

  mkdir -p "$(dirname "$RELEASE_NOTES_PATH")"
  RELEASE_SUBJECTS="$subjects" python3 - "$RELEASE_NOTES_PATH" <<'PY'
import re
import sys
import os
from pathlib import Path

path = Path(sys.argv[1])
subjects = [line.strip() for line in os.environ.get("RELEASE_SUBJECTS", "").splitlines() if line.strip()]

items = []
fallback_items = []
for subject in subjects:
    if subject.lower() in {"init", "initial commit"}:
        continue
    match = re.match(r"^(feat|fix|chore|docs|style|refactor|test|perf|build|ci)(\([^)]+\))?:\s*(.*)", subject)
    kind = match.group(1) if match else ""
    description = match.group(3) if match else subject
    if kind == "chore" and description.startswith("release "):
        continue
    if description:
        item = f"- {description}"
        if kind in {"feat", "fix", "refactor", "perf"} or not kind:
            items.append(item)
            fallback_items.append(item)

if not items:
    items = fallback_items[-3:] if fallback_items else ["- 안정성 개선 및 내부 테스트 업데이트"]

notes = "\n".join(items)
if len(notes) > 500:
    notes = notes[:497].rstrip() + "..."

path.write_text(notes + "\n", encoding="utf-8")
print(f"Generated release notes: {path}")
print(notes)
PY
}

commit_release_metadata() {
  local version_name="$1"

  if [[ "${INTERVALSGYM_SKIP_RELEASE_COMMIT:-0}" == "1" ]]; then
    echo "Skipping release commit because INTERVALSGYM_SKIP_RELEASE_COMMIT=1"
    return
  fi

  git add "$BUILD_GRADLE_PATH" "$RELEASE_NOTES_PATH"
  if git diff --cached --quiet -- "$BUILD_GRADLE_PATH" "$RELEASE_NOTES_PATH"; then
    echo "No release metadata changes to commit."
    return
  fi

  git commit -m "chore: release $version_name" -m "$(cat "$RELEASE_NOTES_PATH")"
}

cd "$ROOT_DIR"
./gradlew clean testDebugUnitTest
ensure_release_targets_clean

if [[ "${INTERVALSGYM_SKIP_VERSION_BUMP:-0}" == "1" ]]; then
  echo "Skipping version bump because INTERVALSGYM_SKIP_VERSION_BUMP=1"
  NEW_VERSION_NAME="$(current_version_name)"
else
  BUMP_OUTPUT="$(bump_version)"
  echo "$BUMP_OUTPUT"
  NEW_VERSION_NAME="$(awk -F= '/^new_version=/{print $2}' <<<"$BUMP_OUTPUT")"
fi

generate_release_notes
commit_release_metadata "$NEW_VERSION_NAME"

./gradlew publishReleaseBundle \
  -Pintervalsgym.signing.storeFile="$SIGNING_STORE_FILE" \
  -Pintervalsgym.signing.storePassword="$SIGNING_STORE_PASSWORD" \
  -Pintervalsgym.signing.keyAlias="$SIGNING_KEY_ALIAS" \
  -Pintervalsgym.signing.keyPassword="$SIGNING_KEY_PASSWORD" \
  -Pintervalsgym.play.serviceAccountJson="$PLAY_SERVICE_ACCOUNT_JSON"
