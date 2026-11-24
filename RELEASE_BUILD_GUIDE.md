# Release APK Build Guide (Revision Universe)

This repository is pre-configured to build installable **Debug APKs** automatically via GitHub Actions (`.github/workflows/build-debug-apk.yml`) or locally with `./gradlew assembleDebug`.

For **Release APKs** (Google Play / production distribution), never commit your release keystore or passwords to Git. Follow the steps below to sign a Release APK safely.

---

## 1. Generate Your Own Upload Keystore (Locally, Never Commit It)

Run this command on your own computer:

```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Keep `my-upload-key.jks`, your keystore password, and key password safe and private.

---

## 2. Build Release APK Locally

Set the environment variables expected by `app/build.gradle.kts` (`signingConfigs.release`):

```bash
export KEYSTORE_PATH="$PWD/my-upload-key.jks"
export STORE_PASSWORD="YOUR_STORE_PASSWORD"
export KEY_PASSWORD="YOUR_KEY_PASSWORD"

./gradlew :app:assembleRelease
```

The signed Release APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

For Google Play App Bundle (`.aab`), run:
```bash
./gradlew :app:bundleRelease
```
Output path: `app/build/outputs/bundle/release/app-release.aab`

---

## 3. Build Release APK on GitHub Actions (Optional)

1. Convert your local `my-upload-key.jks` to Base64:
   ```bash
   base64 -w 0 my-upload-key.jks > keystore.b64
   ```
2. In your GitHub repository, open **Settings → Secrets and variables → Actions → New repository secret** and add:
   - `RELEASE_KEYSTORE_BASE64` — contents of `keystore.b64`
   - `STORE_PASSWORD` — your keystore password
   - `KEY_PASSWORD` — your key password
3. In a workflow job, decode the secret to `my-upload-key.jks`, export `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD`, and run `./gradlew :app:assembleRelease`.
4. Delete temporary `keystore.b64` from your local disk after adding the secret.
