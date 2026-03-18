# PinLevel Release Guide

This document covers how to install PinLevel on an Android device for testing and how to publish it to the Google Play Store.

---

## Part 1: Install on Android Device

### Option A: Android Studio (Recommended)

1. **Download Android Studio** from https://developer.android.com/studio
2. **Install** – it will download the Android SDK and bundled JDK automatically
3. **Open the project**: File → Open → navigate to the PinballLevelTool folder
4. Let Gradle sync (may take a few minutes the first time)

**Run on your phone:**

1. **Enable Developer Options** on your phone:
   - Settings → About Phone → tap "Build Number" 7 times
2. **Enable USB Debugging**: Settings → Developer Options → USB Debugging → On
3. **Connect your phone via USB** and approve the debugging prompt on the device
4. In Android Studio, select your device from the run target dropdown
5. Click the green **Run** button – the app builds and installs directly to your phone

### Option B: Build APK and Sideload

1. In Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. The APK is created at `app/build/outputs/apk/debug/app-debug.apk`
3. Transfer the APK to your phone (email, Google Drive, USB copy, etc.)
4. On your phone, open the APK file and allow "Install from unknown sources" when prompted
5. Complete the installation

---

## Part 2: Publish to Google Play Store

### Step 1: Google Play Developer Account

- Go to https://play.google.com/console
- Sign in with your Google account
- Pay the **one-time $25 registration fee**
- Complete your developer profile (name, email, etc.)
- Approval is usually instant but can take up to 48 hours

### Step 2: Generate Signed Release Bundle

Debug builds cannot be published. You must create a signed release build:

1. In Android Studio: **Build → Generate Signed Bundle / APK**
2. Choose **Android App Bundle** (.aab) – Google requires this format for Play Store
3. **Create a new keystore** (or use existing):
   - Location: e.g. `c:\Users\Johnm\PinballLevel\pinlevel-release.jks`
   - Password: choose a strong password and **save it permanently**
   - Fill in certificate details (your name, organization, etc.)
4. Select **release** build type and click Finish
5. Output: `app/release/app-release.aab`

**Critical:** Back up your `.jks` keystore file and password. If you lose them, you can never update the app on the Play Store.

### Step 3: Store Listing Requirements

In the Play Console, create your app and complete:

| Requirement | Details |
|-------------|---------|
| **App details** | Short description (80 chars), full description (4000 chars), category: Tools |
| **Graphics** | App icon (512×512), feature graphic (1024×500), 2–8 phone screenshots |
| **Content rating** | Complete the questionnaire (PinLevel will rate "Everyone") |
| **Privacy policy** | Required – host a simple policy (GitHub Pages, Google Doc, or your site) |
| **Target audience** | Select age groups; "18+" avoids extra children's privacy requirements |

### Step 4: Suggested Store Content for PinLevel

- **Title:** PinLevel – Pinball Machine Leveler
- **Short description:** Precision leveler for pinball machines with live bubble level and per-machine presets.
- **Full description:** Include keywords: pinball, leveler, level, bubble level, pitch, roll, playfield angle, PinGuy alternative.

### Step 5: Upload and Roll Out

1. In Play Console: Release → Production → Create new release
2. Upload the `.aab` file
3. Add release notes
4. Review and roll out – Google reviews new apps (typically 1–7 days)

---

## Part 3: Cost and Time Summary

| Item | Cost | Time |
|------|------|------|
| Android Studio | Free | ~15 min install |
| USB debugging setup | Free | 2 min |
| Google Developer account | $25 one-time | Instant to 48 hrs |
| Signing key generation | Free | 5 min |
| Store listing + review | Free | 1–7 days |
