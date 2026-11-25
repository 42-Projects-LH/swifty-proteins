# Android Emulator and 42 School Environment Setup

This guide explains how to configure the Android SDK, Gradle cache, and an Android Virtual Device (AVD) emulator—both on a standard development workstation and on a **42 school computer** (where `$HOME` has a strict disk quota and large files must reside in `~/goinfre`).

## Architecture & Makefile Auto-Detection

The project's [`Makefile`](../../Makefile) automatically handles SDK resolution, stale `local.properties` cleanup, and emulator lifecycle management:

- **Automatic `ANDROID_HOME` Detection**: Searches candidate SDK directories in priority order:
  1. `$(HOME)/goinfre/Android/Sdk`
  2. `/goinfre/$(USER)/Android/Sdk`
  3. `/sgoinfre/goinfre/Perso/$(USER)/Android/Sdk`
  4. `$(HOME)/Android/Sdk`
  5. `/usr/lib/android-sdk`
- **Stale `local.properties` Cleanup (`ENSURE_SDK`)**: Before running Gradle tasks, if `local.properties` points to a non-existent `sdk.dir` (e.g., a wiped `goinfre` from a previous session or different machine), `make` automatically removes it so Gradle falls back cleanly to `ANDROID_HOME`.
- **Automatic `PATH` Export**: Prepends `$(ANDROID_HOME)/platform-tools` (`adb`) and `$(ANDROID_HOME)/emulator` (`emulator`) to `PATH`.
- **Auto-Boot Emulator (`make emu` / `make start-emu`)**: If no emulator is currently running (`adb -e get-state`), `make start-emu` (or `make install-emu` / `make emu`) automatically finds the first available AVD (or `AVD=<name>`), launches it in the background, waits until Android finishes booting (`sys.boot_completed == 1`), installs the APK, and launches the app.

## Setting Up on a 42 School PC (`goinfre` Quota-Safe Setup)

On 42 school machines, `$HOME` is stored on NFS with a small quota (typically 5 GB), whereas `~/goinfre` is fast local storage with plenty of free space. Because the Android SDK, Gradle cache (`~/.gradle`), and emulator system images (`~/.android/avd`) require several gigabytes, place them inside `~/goinfre`.

### Step 1: Redirect Gradle, AVD Storage, and SDK Licenses to `~/goinfre`

Run the following block once when logging into a school PC:

```bash
# 1. Stop any running Gradle daemon
./gradlew --stop 2>/dev/null || pkill -f GradleDaemon 2>/dev/null || true

# 2. Create SDK, Gradle, and AVD directories in ~/goinfre
mkdir -p ~/goinfre/Android/Sdk ~/goinfre/.gradle ~/goinfre/.android/avd

# 3. Copy SDK licenses if already present in ~/Android/Sdk, or generate accepted licenses
if [ -d ~/Android/Sdk/licenses ]; then
    cp -r ~/Android/Sdk/licenses ~/goinfre/Android/Sdk/
else
    mkdir -p ~/goinfre/Android/Sdk/licenses
    printf "\n24333f8a63b6825ea9c5514f83c2829b004d1fee" > ~/goinfre/Android/Sdk/licenses/android-sdk-license
fi

# 4. Symlink ~/.gradle to ~/goinfre/.gradle
if [ ! -L ~/.gradle ]; then
    rm -rf ~/.gradle
    ln -s ~/goinfre/.gradle ~/.gradle
fi

# 5. Symlink ~/.android/avd to ~/goinfre/.android/avd
if [ ! -L ~/.android/avd ]; then
    rm -rf ~/.android/avd
    mkdir -p ~/.android
    ln -s ~/goinfre/.android/avd ~/.android/avd
fi
```

> [!NOTE]
> Once `~/goinfre/Android/Sdk/licenses/android-sdk-license` exists, running `make build` will automatically download `build-tools`, `platforms`, and `platform-tools` (`adb`) via Gradle without needing Android Studio.

### Step 2: Install the Android Emulator & Create a Virtual Device (CLI)

If the Android Emulator and a virtual phone (AVD) are not yet installed in `~/goinfre/Android/Sdk`, you can install them 100% from the terminal without opening Android Studio:

```bash
# 1. Download Android command-line tools into ~/goinfre/Android/Sdk
mkdir -p ~/goinfre/Android/Sdk/cmdline-tools
curl -L https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o ~/goinfre/cmdline-tools.zip
unzip -q ~/goinfre/cmdline-tools.zip -d ~/goinfre/Android/Sdk/cmdline-tools
mv ~/goinfre/Android/Sdk/cmdline-tools/cmdline-tools ~/goinfre/Android/Sdk/cmdline-tools/latest
rm -f ~/goinfre/cmdline-tools.zip

# 2. Install the emulator and an x86_64 system image (Android 14 / API 34)
yes | ~/goinfre/Android/Sdk/cmdline-tools/latest/bin/sdkmanager \
    --sdk_root=/goinfre/$USER/Android/Sdk \
    "emulator" \
    "system-images;android-34;google_apis;x86_64" \
    "platforms;android-34"

# 3. Create an Android Virtual Device (AVD) named "pixel"
echo "no" | ~/goinfre/Android/Sdk/cmdline-tools/latest/bin/avdmanager \
    create avd -n pixel \
    -k "system-images;android-34;google_apis;x86_64" \
    --device "pixel_6" \
    --force
```

## Launching and Testing on the Emulator

### Build, Auto-Boot Emulator, Install, and Launch

Once an AVD (such as `pixel`) exists, a single command compiles the APK, boots the emulator if it is not already running, waits for the Android OS to finish booting, installs the APK, and starts **Swifty Proteins**:

```bash
make start-emu
```

To target a specific AVD by name when multiple virtual devices exist:

```bash
make start-emu AVD=pixel
```

To boot the emulator alone without installing or launching the app:

```bash
make emu
```

## Simulating Biometrics on the Emulator

To test Fingerprint / Biometric authentication ([`feat-002-login-view.md`](feat-002-login-view.md)) on the emulator:

1. **Enroll a Virtual Fingerprint (First-Time Setup on AVD)**:
   - On the emulator, open **Settings → Security & privacy → Device unlock → Fingerprint** (or **Screen lock & fingerprint**).
   - Set a fallback PIN (e.g., `1234`).
   - When prompted to **"Touch the sensor"** during fingerprint enrollment, run:
     ```bash
     make bio-success
     ```
     Repeat until enrollment reaches 100%.

2. **Test Biometric Login Inside Swifty Proteins**:
   - When the `BiometricPrompt` dialog is displayed on screen:
     ```bash
     # Simulate enrolled fingerprint (ID 1) -> Login succeeds
     make bio-success

     # Simulate unrecognized fingerprint (ID 2) -> Error shake / failure feedback
     make bio-fail
     ```

## Emulator Makefile Commands Reference

| Command | Description |
|---|---|
| `make emu` | Auto-detect and boot the Android emulator in the background if not already running |
| `make emu AVD=pixel` | Boot a specific Android Virtual Device (`pixel`) and wait until ready |
| `make install-emu` | Build debug APK, auto-boot emulator if needed, and install APK (`adb -e install -r -d`) |
| `make start-emu` | Build debug APK, auto-boot emulator if needed, install APK, and cold-launch `SplashActivity` |
| `make run-emu` | Cold-launch `SplashActivity` on the running emulator without rebuilding |
| `make bio-success` | Send valid fingerprint event (`adb -e emu finger touch 1`) |
| `make bio-fail` | Send invalid fingerprint event (`adb -e emu finger touch 2`) |
| `make devices` | List all connected emulators and physical USB devices (`adb devices -l`) |
| `make check-sdk` | Display project SDK configuration, detected `ANDROID_HOME` platforms, and Java runtime |
| `make inspect-prefs` | List `shared_prefs/` and inspect encrypted `secure_user_prefs.xml` ciphertext on device |
