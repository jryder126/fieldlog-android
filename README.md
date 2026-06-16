# Fieldlog — Android (Android Studio project)

A native Android wrapper (Kotlin + WebView) around the Fieldlog 8-week
strength + running tracker. The whole app lives in `app/src/main/assets/`
and runs fully offline. No internet permission is requested.

## Get an APK download link (no Android Studio needed)

This project includes a GitHub Actions workflow (`.github/workflows/build-apk.yml`)
that builds the APK on GitHub's servers and publishes it for download.

1. Create a new repository on GitHub (private is fine).
2. Upload **all** of these files to it, keeping the folder structure. From a
   computer: `git init && git add . && git commit -m "init" && git branch -M main`,
   add your remote, then `git push`. (Web drag-and-drop also works if you keep
   the folders intact.)
3. Open the repo's **Actions** tab. "Build APK" runs automatically on push, or
   click **Run workflow**. It takes a few minutes.
4. Download the finished APK:
   - **Permanent link:** `https://github.com/<you>/<repo>/releases/latest` →
     `app-debug.apk`.
   - or **Actions run → Artifacts → fieldlog-debug-apk**.
5. Open that link on your phone, download, and install (allow "install unknown
   apps" when prompted).

The APK is signed with Android's debug key — fine for your own devices. For the
Play Store you'd add a real signing key.

## Build & run
1. Open **Android Studio** (Hedgehog or newer) → **Open** → select this folder
   (`FieldlogAndroid`).
2. Let it sync. If prompted, accept the Android Gradle Plugin / Gradle wrapper
   download and install any missing SDK components (compileSdk 34).
3. Plug in your phone with USB debugging on (or use an emulator) and press
   **Run ▶**. The app installs and launches.

To make an installable file: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
The debug APK lands in `app/build/outputs/apk/debug/`. For a shareable release
build, set up a signing key (**Build → Generate Signed Bundle / APK**).

## How the backup feature works natively
- **Export**: the in-app Export button calls a small JavaScript bridge
  (`AndroidBridge.export`) which opens Android's "Save file" dialog so you choose
  where the `.json` lands. (See `MainActivity.kt`.)
- **Import**: the in-app Import button uses a standard file input, handled by
  `onShowFileChooser`, which opens the system file picker.

Your data persists in the app's WebView storage. Still export occasionally —
clearing app data wipes local storage.

## Updating the app later
Replace the files in `app/src/main/assets/` with a newer version, bump
`versionCode` / `versionName` in `app/build.gradle.kts`, and rebuild. Your
exported `.json` backups import straight into the new version.

## Project notes
- Kotlin, single Activity (`com.fieldlog.app.MainActivity`), no Compose.
- `minSdk 24`, `targetSdk 34`, `compileSdk 34`, Java/JVM 17.
- A Gradle **wrapper jar is not included**; Android Studio provides Gradle when
  you open the project. If you prefer the command line, run
  `gradle wrapper` once (with a local Gradle) to generate `gradlew`.
