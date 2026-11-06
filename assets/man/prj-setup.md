# Project Setup

## Prerequisites

### 1. Java Development Kit (JDK)
* **JDK 21** (or JDK 17).
> [!IMPORTANT]
> Gradle 9.6.0 requires Java 17–25. Newer versions (such as Java 27) are not supported by the Gradle daemon and will cause the build to fail.
* **Arch Linux setup**:
  ```bash
  sudo pacman -S jdk21-openjdk
  sudo archlinux-java set java-21-openjdk
  ```

### 2. Android Studio & Android SDK
* [Android Studio](https://developer.android.com/studio) (for this project, version `android-studio-quail4-patch1-linux.tar.gz` or AUR `android-studio` was used).
* **Android SDK**:
  * Android SDK Platform: API 37 (compatible with API 34–35)
  * Android SDK Build-Tools & Platform-Tools
  * Android Emulator & system image (e.g. Pixel with API 35)

### 3. Android Tools & Device Permissions (Linux)
* **Packages**: `android-tools` (provides `adb`, `fastboot`) and `android-udev` (udev rules for physical device debugging over USB).
  ```bash
  sudo pacman -S android-tools android-udev
  ```
* **User groups**: Add user to `kvm` (for hardware-accelerated emulator virtualization) and `adbusers` (for USB device access):
  ```bash
  sudo usermod -aG kvm,adbusers $USER
  ```

### 4. Environment Variables
Add the following exports to your shell configuration (`~/.zshrc` or `~/.bashrc`):
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
```

### 5. Wayland / Hyprland Notes
* **IDE**: Android Studio runs smoothly under Xwayland or Wayland.
* **Emulator**: Hardware acceleration requires `/dev/kvm` and membership in the `kvm` group. If the standalone emulator experiences rendering or scaling glitches under Wayland/Hyprland, launch it with Xwayland fallback:
  ```bash
  QT_QPA_PLATFORM=xcb emulator @<avd_name>
  ```

---

## Setup
1. Create or open the project in **Android Studio**.
2. Select **Empty Views Activity** (this uses standard XML Views, making integration with 3D views like SceneView/Filament much simpler).
![img](/assets/imgs/sp001.png)
