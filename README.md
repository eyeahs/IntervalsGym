# IntervalsGym

Intervals.icu training companion for Android.

## GitHub Actions OAuth Build

The repository does not store Intervals.icu OAuth values in source control. The
debug APK that includes OAuth configuration can be built from GitHub Actions.

1. Open GitHub repository settings.
2. Go to `Settings` -> `Secrets and variables` -> `Actions`.
3. Add these repository secrets:
   - `INTERVALS_OAUTH_CLIENT_ID`
   - `INTERVALS_OAUTH_CLIENT_SECRET`
4. Open `Actions` -> `Android OAuth Build`.
5. Click `Run workflow`.
6. After the workflow finishes, download the `IntervalsGym-debug-apk` artifact.

The workflow passes the secrets to Gradle at build time and generates native
bridge libraries during the build. Generated native sources and APK outputs are
not committed to the repository.

## Play Internal Test Deployment

The `Publish Play Internal Test` workflow builds a signed release AAB and
publishes it to the Google Play internal testing track.

Add these repository secrets in `Settings` -> `Secrets and variables` ->
`Actions`:

- `INTERVALS_OAUTH_CLIENT_ID`
- `INTERVALS_OAUTH_CLIENT_SECRET`
- `ANDROID_SIGNING_KEYSTORE_BASE64`
- `ANDROID_SIGNING_STORE_PASSWORD`
- `ANDROID_SIGNING_KEY_ALIAS`
- `ANDROID_SIGNING_KEY_PASSWORD`
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

Create the keystore secret from the upload keystore:

```bash
base64 -i /path/to/upload-keystore.jks | pbcopy
```

Paste the copied value into `ANDROID_SIGNING_KEYSTORE_BASE64`. Paste the full
Google Play service account JSON into `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`.

To publish:

1. Open `Actions` -> `Publish Play Internal Test`.
2. Click `Run workflow`.
3. Optionally provide `version_code`, `version_name`, and `release_notes`.
4. Leave `version_code` blank to use `100000 + GitHub run number`.
5. Leave `version_name` blank to bump the source patch version by the GitHub run
   number.

The workflow uploads the release bundle to the `internal` track and also stores
the generated AAB as a workflow artifact.

## Local Build

For local development, create a private properties file outside the repository:

```properties
intervals.clientId=YOUR_CLIENT_ID
intervals.clientSecret=YOUR_CLIENT_SECRET
intervals.redirectScheme=intervalsgym
intervals.redirectHost=intervals-oauth
```

By default the Gradle build looks for:

```text
/Users/hyunwoo.pr/Dev/private_settings/intervalsgym_oauth.properties
```

You can also pass another file path:

```bash
./gradlew assembleDebug \
  -Pintervalsgym.oauth.properties=/path/to/intervalsgym_oauth.properties
```

Run tests with:

```bash
./gradlew testDebugUnitTest
```
