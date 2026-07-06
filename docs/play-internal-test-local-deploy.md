# Local Play Internal Test Deployment

This project can publish a signed release App Bundle directly to the Google Play
internal testing track from a local machine. The Play track is configured in
`app/build.gradle.kts`:

- `track.set("internal")`
- `releaseStatus.set(COMPLETED)`
- service account path from `intervalsgym.play.serviceAccountJson`

## Private Config

Keep release signing and Play credentials outside the repository. The local
deployment used this private config file:

```text
/Users/hyunwoo.pr/Dev/private_settings/intervalsgym_publish_config.json
```

Expected JSON keys:

```text
signingStoreFile
signingStorePassword
signingKeyAlias
signingKeyPassword
playServiceAccountJson
```

Do not print these values in terminal output. Pass them to Gradle through
`ORG_GRADLE_PROJECT_...` environment variables for the single command run.

## Before Publishing

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`.
   Google Play rejects any `versionCode` that was already uploaded.
2. Update release notes in:

```text
app/src/main/play/release-notes/ko-KR/internal.txt
```

3. Optional but recommended: run the test suites before releasing.

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

## Build Signed Release Bundle

Use the private JSON to inject signing and Play properties without exposing
secret values:

```bash
python3 - <<'PY'
import json, os, subprocess, sys
from pathlib import Path

config = json.loads(
    Path('/Users/hyunwoo.pr/Dev/private_settings/intervalsgym_publish_config.json').read_text()
)
env = os.environ.copy()
env.update({
    'ORG_GRADLE_PROJECT_intervalsgym.signing.storeFile': config['signingStoreFile'],
    'ORG_GRADLE_PROJECT_intervalsgym.signing.storePassword': config['signingStorePassword'],
    'ORG_GRADLE_PROJECT_intervalsgym.signing.keyAlias': config['signingKeyAlias'],
    'ORG_GRADLE_PROJECT_intervalsgym.signing.keyPassword': config['signingKeyPassword'],
    'ORG_GRADLE_PROJECT_intervalsgym.play.serviceAccountJson': config['playServiceAccountJson'],
})

sys.exit(subprocess.run(['./gradlew', 'bundleRelease'], env=env).returncode)
PY
```

Expected output artifact:

```text
app/build/outputs/bundle/release/app-release.aab
```

## Publish To Internal Test

Run the Play Publisher task with the same environment injection:

```bash
python3 - <<'PY'
import json, os, subprocess, sys
from pathlib import Path

config = json.loads(
    Path('/Users/hyunwoo.pr/Dev/private_settings/intervalsgym_publish_config.json').read_text()
)
env = os.environ.copy()
env.update({
    'ORG_GRADLE_PROJECT_intervalsgym.signing.storeFile': config['signingStoreFile'],
    'ORG_GRADLE_PROJECT_intervalsgym.signing.storePassword': config['signingStorePassword'],
    'ORG_GRADLE_PROJECT_intervalsgym.signing.keyAlias': config['signingKeyAlias'],
    'ORG_GRADLE_PROJECT_intervalsgym.signing.keyPassword': config['signingKeyPassword'],
    'ORG_GRADLE_PROJECT_intervalsgym.play.serviceAccountJson': config['playServiceAccountJson'],
})

sys.exit(subprocess.run(['./gradlew', 'publishReleaseBundle'], env=env).returncode)
PY
```

Successful output includes:

```text
App Bundle upload complete
Updating [completed] release (com.lighthousepark.intervalsgym:[VERSION_CODE]) in track 'internal'
Committing changes
BUILD SUCCESSFUL
```

## Last Known Successful Local Upload

- Date: 2026-07-06
- Version: `1.3.11`
- Version code: `15`
- Track: `internal`
- Artifact: `app/build/outputs/bundle/release/app-release.aab`
- Final task: `./gradlew publishReleaseBundle`

## Deployment Log

### 2026-07-06

- Purpose: internal test release for running TCX heart rate upload fix.
- Version: `1.3.11`
- Version code: `15`
- Private config: `/Users/hyunwoo.pr/Dev/private_settings/intervalsgym_publish_config.json`
- Signing key: injected from private config; secret values were not printed.
- Build verification: `./gradlew build` with release signing properties injected from private config.
- Build result: `BUILD SUCCESSFUL`
- Publish command: `./gradlew publishReleaseBundle` with signing and Play service account properties injected from private config.
- Publish result: `App Bundle upload complete`
- Play update: `Updating [completed] release (com.lighthousepark.intervalsgym:[15]) in track 'internal'`
- Play commit result: `Committing changes`

## Common Failure

If Play rejects the upload with:

```text
Version code is too low or has already been used
```

increase `versionCode`, update `versionName` if appropriate, regenerate the
release notes, and rerun `publishReleaseBundle`.

If `:app:signReleaseBundle` fails with a null signing error, the release signing
Gradle properties were not provided. Check the private config path and make sure
the command injects all `ORG_GRADLE_PROJECT_intervalsgym.signing.*` values.
