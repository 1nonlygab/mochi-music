# Signing Mochi releases

Android updates must use the **same application ID and signing key** as the installed APK. Keep an encrypted offline backup of the keystore and its password. If they are lost, a new key cannot update existing installs. Never commit the keystore, private key, passwords, or their base64 encoding to Git, and never paste them into issues or chat.

A release build fails rather than silently using the debug key if signing is missing. Debug APKs use a different application ID and are not release updates.

## One-time setup on a trusted computer

1. Install JDK 21 and create a keystore **once**, outside the repository, using an interactive prompt so the password does not enter shell history:

   ```sh
   keytool -genkeypair -v -keystore mochi-release.keystore -storetype PKCS12 \
     -alias mochi -keyalg RSA -keysize 4096 -validity 10000
   ```

   Choose a strong password and store it in a password manager. Back up the keystore separately in a secure location. Do not generate another key for later updates.
2. In the repository's **Settings → Secrets and variables → Actions**, create four repository secrets: `MOCHI_KEYSTORE_BASE64` (base64 of the entire keystore), `MOCHI_STORE_PASSWORD`, `MOCHI_KEY_ALIAS` (`mochi` if using the command above), and `MOCHI_KEY_PASSWORD` (for PKCS12, typically the same password). Generate the base64 locally without logging or committing it. Only trusted repository administrators should have access to the keystore or the ability to change the workflow.
3. Run **Actions → Build signed Mochi APK** on `main`. It builds one universal FOSS APK, verifies its signature, and uploads only the APK as a temporary Actions artifact. It does not publish a release. A GitHub Actions artifact expires; download and store the APK separately if needed.

For future updates use the same keystore, increment `versionCode` and `versionName` in `app/build.gradle.kts`, and rebuild. If publishing an update in the app, attach the signed universal APK to a newer GitHub release in **this** repository. Verify the certificate fingerprint against the first release before distributing it. The app's updater reads releases from `1nonlygab/mochi-music`, not upstream.

## Local release build

Place the same keystore at `app/keystore/release.keystore` (this directory is gitignored), provide `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` through a secure environment, then run `./gradlew assembleUniversalFossRelease`. The file is under `app/build/outputs/apk/universalFoss/release/`. Ensure secret variables are not recorded in shell history or logs.
