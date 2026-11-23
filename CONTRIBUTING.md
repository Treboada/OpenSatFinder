# Contributing to OpenSatFinder

OpenSatFinder is free software under the GPL-3.0-or-later. Contributions are welcome:
bug reports, translations, code, and documentation. Everything in the repository - code,
comments, commit messages, issues - is written in English.

## Before you start

- JDK 21 (Temurin 21, or the JDK Android Studio bundles).
- Android SDK with platform `android-37` and build-tools `37.0.0`:

  ```bash
  sdkmanager "platforms;android-37" "build-tools;37.0.0"
  ```

  Export `ANDROID_HOME`, or write `sdk.dir` into `local.properties` (git-ignored).
- Git.

Android Studio provides the JDK and the SDK. The committed wrapper downloads Gradle
9.9.1 on the first run; `./gradlew` is the only Gradle you call - never a host one.

## Build and test

```bash
./gradlew :app:assembleDebug      # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest  # JVM tests, seconds
./gradlew :app:lintDebug          # Android lint
./gradlew :app:ktlintCheck        # Kotlin style
./gradlew :app:assembleRelease    # unsigned release APK, what F-Droid builds
```

The instrumented tests need an emulator or a device:

```bash
./gradlew :app:assembleDebugAndroidTest   # compile them, no device needed
./gradlew :app:connectedDebugAndroidTest  # run them
```

An x86_64 emulator needs `/dev/kvm` on Linux; without it, run the JVM tests and say so
instead of claiming the instrumented ones passed.

Before sending a change, this has to pass:

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:ktlintCheck
```

## Where the code goes

- **Domain logic** lives in `app/src/main/java/<package>/compass/`,
  `pointing/`, `location/` and `sound/`, and is pure Kotlin: no Android framework, so
  its tests in `app/src/test/` run in seconds. Prefer adding logic there.
- **Platform access** stays in small classes beside it (`CompassSensorSource`,
  `rememberLocationController()`), never inside composables.
- **Compose screens** live in `app/src/main/java/<package>/ui/`, are stateless and take
  a state value. Their tests live in `app/src/androidTest/`.
- **Dependencies**: a version in `[versions]`, a library in `[libraries]`, then
  `libs.<alias>` in the build file. Never a version inline. The app declares no
  network permission, and a dependency that needs one is a design decision, not a
  detail.

Styles follow `.editorconfig`: ktlint's rules, with Compose functions exempt from the
lowercase naming rule. `./gradlew :app:ktlintFormat` rewrites the files; check the
diff before committing it.

Every Kotlin file opens with a copyright line and an `SPDX-License-Identifier` tag.

## Translations and the store listing

The listing is code too. It lives in `fastlane/metadata/android/`, with a directory per
language: `en-US`, `es-ES`, `ca`, `de`, `eu`, `fr`, `gl` and `pt`. `full_description.txt`
holds the store text; `title.txt`, `short_description.txt` and the changelog have hard
limits, and `FastlaneListingTest` fails the build when one is crossed or a language
falls behind.

Adding a language means adding the app's strings in `app/src/main/res/values-<tag>/`
and its listing directory, both. A translation without the other half ships a screen
nobody can read or a listing nobody can find.

## Releasing

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Add `fastlane/metadata/android/<locale>/changelogs/<versionCode>.txt` for every
   language; `FastlaneListingTest` fails without it.
3. Commit, tag the commit with the version (`1.0.0`), push the tag.
4. Publish a GitHub release for that tag. `.github/workflows/release.yml` checks that
   the tag matches `versionName`, runs the unit tests, builds `:app:assembleRelease`,
   signs the APK with `apksigner` and attaches `OpenSatFinder-<version>.apk`.
5. `.github/workflows/fdroid-repo.yml` indexes the new APK into the F-Droid
   repository published on GitHub Pages, described below.

Moving or re-cutting a tag means deleting the release first, then the tag, then
re-tagging: a published release is not edited, it is replaced.

### Signing

The Gradle build has no signing configuration: it produces an unsigned release APK, and
the workflow signs it afterwards with a key that lives in repository secrets. That is
what lets F-Droid rebuild the same tag and sign it with its own key.

The key is created once, outside the repository:

```bash
keytool -genkeypair -keystore release.jks -alias opensatfinder \
    -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=OpenSatFinder"
base64 -w0 release.jks > release.jks.b64   # macOS: base64 -i release.jks | tr -d '\n'
```

Then four secrets under **Settings -> Secrets and variables -> Actions**:

- `KEYSTORE_BASE64` - the contents of `release.jks.b64`, one line.
- `KEYSTORE_PASSWORD` - the keystore password.
- `KEY_ALIAS` - `opensatfinder`.
- `KEY_PASSWORD` - the key password (the same one for a PKCS12 keystore).

Keep `release.jks` and its passwords out of git and back them up: without that key you
cannot update an installed copy. The repository on GitHub Pages ships that same APK, so
there is no second signature to keep track of.

### Own F-Droid repository

The main F-Droid repository did not take the app yet, because a project with a single
fresh release has no maintenance history. Until it does, GitHub Pages serves an
F-Droid repository of our own under
`https://treboada.github.io/OpenSatFinder/repo`. `.github/workflows/fdroid-repo.yml`
indexes the APKs attached to the GitHub releases, signs the index and force-pushes the
result to the `gh-pages` branch; it runs after every published release and can be
started by hand from the Actions tab.

The index is signed with a second key inside the same keystore, under the alias
`fdroidrepo`. Add it once to `release.jks`, outside the repository:

```bash
keytool -genkeypair -keystore release.jks -alias fdroidrepo \
    -keyalg RSA -keysize 4096 -validity 10000 -storetype pkcs12 \
    -dname "CN=OpenSatFinder repository"
base64 -w0 release.jks > release.jks.b64   # macOS: base64 -i release.jks | tr -d '\n'
keytool -list -v -keystore release.jks -alias fdroidrepo | grep SHA256
```

Then re-upload `release.jks.b64` as `KEYSTORE_BASE64`. The release workflow keeps signing
the APK with the `opensatfinder` alias, and the repository workflow signs the index with
`fdroidrepo`; both use `KEYSTORE_PASSWORD`, because a PKCS12 keystore has a single
password. No other secret is needed.

If `keytool -list -v` reports a JKS keystore whose key password differs from the store
password, convert it first, because the workflow passes `KEYSTORE_PASSWORD` for both:

```bash
keytool -importkeystore -srckeystore release.jks -destkeystore release-p12.jks \
    -deststoretype pkcs12
```

And **Settings -> Pages -> Source: Deploy from a branch**, branch `gh-pages`, folder
`/ (root)`. The fingerprint that `keytool` printed is what the F-Droid client asks for
when the repository is added. Keep the keystore and its password: a new index key means
every installed client rejects the repository, and losing the file loses both
signatures at once.

## Licence

By contributing you agree to license your work under the GPL-3.0-or-later, the terms in
[LICENSE](LICENSE). The satellite names and slots come from the CelesTrak GEO listing,
credited in the app's About screen; code, text and the launcher icon are covered by the
same licence unless a separate agreement says otherwise.
