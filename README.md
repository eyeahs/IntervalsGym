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
