# ============================================================
#  VARIABLES
# ============================================================

SHELL		:= /bin/bash

PACKAGE		:= com.example.swiftyproteins
MAIN_ACTIVITY	:= $(PACKAGE)/.ui.splash.SplashActivity
APK_DEBUG	:= app/build/outputs/apk/debug/app-debug.apk
APK_RELEASE	:= app/build/outputs/apk/release/app-release-unsigned.apk
GRADLE		:= ./gradlew
SCREENSHOT_DIR	:= screenshots

# Auto-detect ANDROID_HOME if not already exported in the environment
ifndef ANDROID_HOME
ifneq ($(wildcard $(HOME)/goinfre/Android/Sdk),)
	export ANDROID_HOME := $(HOME)/goinfre/Android/Sdk
else ifneq ($(wildcard /goinfre/$(USER)/Android/Sdk),)
	export ANDROID_HOME := /goinfre/$(USER)/Android/Sdk
else ifneq ($(wildcard /sgoinfre/goinfre/Perso/$(USER)/Android/Sdk),)
	export ANDROID_HOME := /sgoinfre/goinfre/Perso/$(USER)/Android/Sdk
else ifneq ($(wildcard $(HOME)/Android/Sdk),)
	export ANDROID_HOME := $(HOME)/Android/Sdk
else ifneq ($(wildcard /usr/lib/android-sdk),)
	export ANDROID_HOME := /usr/lib/android-sdk
endif
endif

# Helper to remove stale local.properties (e.g. pointing to cleared goinfre or another PC)
ENSURE_SDK = @if [ -f local.properties ]; then \
	SDK_DIR=$$(sed -n 's/^sdk\.dir=//p' local.properties | head -n 1); \
	if [ -n "$$SDK_DIR" ] && [ ! -d "$$SDK_DIR" ]; then \
		$(ECHO) ">>> $(YELLOW)Removing stale local.properties (missing: $$SDK_DIR)...$(RESET)"; \
		$(RM) local.properties; \
	fi; \
fi

# Device targeting:
# Pass DEVICE=<serial> to target a specific device, e.g.:
#   make install DEVICE=ASUS_I006D
ifdef DEVICE
	ADB_TARGET	:= -s $(DEVICE)
else
	ADB_TARGET	:=
endif

ADB		:= adb $(ADB_TARGET)
ADB_USB		:= adb -d
ADB_EMU		:= adb -e

# ------------------------------------------------------------
#  General Variables
# ------------------------------------------------------------

ECHO     := echo
FIND     := /bin/find
IGNORE   := 2>/dev/null || true
MV       := /bin/mv
MKDIR    := mkdir -p
RM       := rm -rf
DATE     := date

# ------------------------------------------------------------
#  Ansi Colors (TrueColor / Pre-evaluated escapes)
# ------------------------------------------------------------

RESET    := $(shell printf '\033[0m')
BLACK    := $(shell printf '\033[1;90m')
RED      := $(shell printf '\033[1;38;2;255;95;95m')
GREEN    := $(shell printf '\033[1;38;2;80;230;120m')
YELLOW   := $(shell printf '\033[1;38;2;255;215;0m')
BLUE     := $(shell printf '\033[1;38;2;90;160;255m')
MAGENTA  := $(shell printf '\033[1;38;2;215;135;255m')
CYAN     := $(shell printf '\033[1;38;2;0;225;255m')
WHITE    := $(shell printf '\033[1;97m')

# ============================================================
#  RULES
# ============================================================

.PHONY: all build release install install-usb install-emu \
        start start-usb start-emu run run-usb run-emu \
        stop restart clear-data clear-data-usb uninstall uninstall-usb \
        bio-success bio-fail logs logs-usb logcat-clear devices \
        screenshot pull-photos shell test lint clean fclean re check-sdk check-cif help

# ------------------------------------------------------------
#  all — Default target: Build debug APK
# ------------------------------------------------------------

all: build

# ------------------------------------------------------------
#  build — Compile debug APK
# ------------------------------------------------------------

build:
	$(ENSURE_SDK)
	@$(ECHO) ">>> $(YELLOW)Building debug APK...$(RESET)"
	@$(GRADLE) assembleDebug
	@$(ECHO) ">>> $(GREEN)Debug APK built at $(APK_DEBUG)$(RESET)"

# ------------------------------------------------------------
#  release — Compile unsigned release APK
# ------------------------------------------------------------

release:
	$(ENSURE_SDK)
	@$(ECHO) ">>> $(YELLOW)Building release APK...$(RESET)"
	@$(GRADLE) assembleRelease
	@$(ECHO) ">>> $(GREEN)Release APK built at $(APK_RELEASE)$(RESET)"

# ------------------------------------------------------------
#  install-usb — Install debug APK on USB-connected phone (-d)
# ------------------------------------------------------------

install-usb: build
	@$(ECHO) ">>> $(YELLOW)Installing APK on USB-connected phone...$(RESET)"
	@$(ADB_USB) install -r -d $(APK_DEBUG)
	@$(ECHO) ">>> $(GREEN)Installation on USB phone successful.$(RESET)"

# ------------------------------------------------------------
#  install-emu — Install debug APK on running emulator (-e)
# ------------------------------------------------------------

install-emu: build
	@$(ECHO) ">>> $(YELLOW)Installing APK on Android emulator...$(RESET)"
	@$(ADB_EMU) install -r -d $(APK_DEBUG)
	@$(ECHO) ">>> $(GREEN)Installation on emulator successful.$(RESET)"

# ------------------------------------------------------------
#  install — Install debug APK on default / active ADB target
# ------------------------------------------------------------

install: build
	@$(ECHO) ">>> $(YELLOW)Installing APK on active ADB target...$(RESET)"
	@$(ADB) install -r -d $(APK_DEBUG)
	@$(ECHO) ">>> $(GREEN)Installation successful.$(RESET)"

# ------------------------------------------------------------
#  start-usb — Build, install, and launch app on USB phone
# ------------------------------------------------------------

start-usb: install-usb run-usb
	@$(ECHO) ">>> $(GREEN)Swifty Proteins running on USB phone.$(RESET)"

# ------------------------------------------------------------
#  start-emu — Build, install, and launch app on emulator
# ------------------------------------------------------------

start-emu: install-emu run-emu
	@$(ECHO) ">>> $(GREEN)Swifty Proteins running on emulator.$(RESET)"

# ------------------------------------------------------------
#  start — Build, install, and launch app on active target
# ------------------------------------------------------------

start: install run
	@$(ECHO) ">>> $(GREEN)Swifty Proteins running on active target.$(RESET)"

# ------------------------------------------------------------
#  run-usb — Cold-launch SplashActivity on USB phone
# ------------------------------------------------------------

run-usb:
	@$(ECHO) ">>> $(YELLOW)Launching $(MAIN_ACTIVITY) on USB phone...$(RESET)"
	@$(ADB_USB) shell am start -S -n $(MAIN_ACTIVITY)
	@$(ECHO) ">>> $(CYAN)Done.$(RESET)"

# ------------------------------------------------------------
#  run-emu — Cold-launch SplashActivity on emulator
# ------------------------------------------------------------

run-emu:
	@$(ECHO) ">>> $(YELLOW)Launching $(MAIN_ACTIVITY) on emulator...$(RESET)"
	@$(ADB_EMU) shell am start -S -n $(MAIN_ACTIVITY)
	@$(ECHO) ">>> $(CYAN)Done.$(RESET)"

# ------------------------------------------------------------
#  run — Cold-launch SplashActivity on active target
# ------------------------------------------------------------

run:
	@$(ECHO) ">>> $(YELLOW)Launching $(MAIN_ACTIVITY)...$(RESET)"
	@$(ADB) shell am start -S -n $(MAIN_ACTIVITY)
	@$(ECHO) ">>> $(CYAN)Done.$(RESET)"

# ------------------------------------------------------------
#  stop — Force-stop running application
# ------------------------------------------------------------

stop:
	@$(ECHO) ">>> $(YELLOW)Stopping $(PACKAGE)...$(RESET)"
	@$(ADB) shell am force-stop $(PACKAGE)
	@$(ECHO) ">>> $(CYAN)Application stopped.$(RESET)"

# ------------------------------------------------------------
#  restart — Stop and cold-launch application
# ------------------------------------------------------------

restart: stop run

# ------------------------------------------------------------
#  clear-data — Wipe app storage, credentials & KeyStore
# ------------------------------------------------------------

clear-data:
	@$(ECHO) ">>> $(YELLOW)Resetting app data & KeyStore credentials...$(RESET)"
	@$(ADB) shell pm clear $(PACKAGE)
	@$(ECHO) ">>> $(GREEN)App state reset to clean slate.$(RESET)"

# ------------------------------------------------------------
#  clear-data-usb — Wipe app storage on USB phone
# ------------------------------------------------------------

clear-data-usb:
	@$(ECHO) ">>> $(YELLOW)Resetting app data on USB phone...$(RESET)"
	@$(ADB_USB) shell pm clear $(PACKAGE)
	@$(ECHO) ">>> $(GREEN)USB device app state reset.$(RESET)"

# ------------------------------------------------------------
#  uninstall — Completely remove package from active target
# ------------------------------------------------------------

uninstall:
	@$(ECHO) ">>> $(RED)Uninstalling $(PACKAGE)...$(RESET)"
	@$(ADB) uninstall $(PACKAGE) $(IGNORE)
	@$(ECHO) ">>> $(CYAN)Uninstalled.$(RESET)"

# ------------------------------------------------------------
#  uninstall-usb — Completely remove package from USB phone
# ------------------------------------------------------------

uninstall-usb:
	@$(ECHO) ">>> $(RED)Uninstalling $(PACKAGE) from USB phone...$(RESET)"
	@$(ADB_USB) uninstall $(PACKAGE) $(IGNORE)
	@$(ECHO) ">>> $(CYAN)Uninstalled from USB device.$(RESET)"

# ------------------------------------------------------------
#  bio-success — Simulate successful fingerprint touch on emulator
# ------------------------------------------------------------

bio-success:
	@$(ECHO) ">>> $(GREEN)Sending valid fingerprint event (ID 1)...$(RESET)"
	@$(ADB_EMU) emu finger touch 1

# ------------------------------------------------------------
#  bio-fail — Simulate unrecognized fingerprint touch on emulator
# ------------------------------------------------------------

bio-fail:
	@$(ECHO) ">>> $(RED)Sending unrecognized fingerprint event (ID 2)...$(RESET)"
	@$(ADB_EMU) emu finger touch 2

# ------------------------------------------------------------
#  logs — Stream filtered logcat for SwiftyProteins
# ------------------------------------------------------------

logs:
	@$(ECHO) ">>> $(YELLOW)Streaming logs for $(PACKAGE) (Ctrl+C to stop)...$(RESET)"
	@$(ADB) logcat -v time | grep --line-buffered -E "SwiftyProteins|AndroidRuntime|FATAL|ShaderHelper|ProteinSurfaceView"

# ------------------------------------------------------------
#  logs-usb — Stream filtered logcat from USB phone
# ------------------------------------------------------------

logs-usb:
	@$(ECHO) ">>> $(YELLOW)Streaming logs from USB phone (Ctrl+C to stop)...$(RESET)"
	@$(ADB_USB) logcat -v time | grep --line-buffered -E "SwiftyProteins|AndroidRuntime|FATAL|ShaderHelper|ProteinSurfaceView"

# ------------------------------------------------------------
#  logcat-clear — Clear device logcat buffer
# ------------------------------------------------------------

logcat-clear:
	@$(ECHO) ">>> $(YELLOW)Clearing logcat buffer...$(RESET)"
	@$(ADB) logcat -c
	@$(ECHO) ">>> $(CYAN)Logcat buffer cleared.$(RESET)"

# ------------------------------------------------------------
#  devices — List connected USB devices and emulators
# ------------------------------------------------------------

devices:
	@$(ECHO) ">>> $(CYAN)Connected ADB Devices:$(RESET)"
	@adb devices -l

# ------------------------------------------------------------
#  screenshot — Capture device screen to screenshots/
# ------------------------------------------------------------

screenshot:
	@$(MKDIR) $(SCREENSHOT_DIR)
	@TIMESTAMP=$$($(DATE) +%Y%m%d_%H%M%S); \
	FILE="$(SCREENSHOT_DIR)/screen_$${TIMESTAMP}.png"; \
	$(ECHO) ">>> $(YELLOW)Capturing device screen to $${FILE}...$(RESET)"; \
	$(ADB) exec-out screencap -p > "$${FILE}"; \
	$(ECHO) ">>> $(GREEN)Screenshot saved: $${FILE}$(RESET)"

# ------------------------------------------------------------
#  pull-photos — Pull exported 3D snapshots from device gallery
# ------------------------------------------------------------

pull-photos:
	@$(MKDIR) $(SCREENSHOT_DIR)/device_gallery
	@$(ECHO) ">>> $(YELLOW)Pulling saved snapshots from Pictures/SwiftyProteins/...$(RESET)"
	-@$(ADB) pull /sdcard/Pictures/SwiftyProteins/. $(SCREENSHOT_DIR)/device_gallery/ $(IGNORE)
	@$(ECHO) ">>> $(CYAN)Snapshots synced to $(SCREENSHOT_DIR)/device_gallery/$(RESET)"

# ------------------------------------------------------------
#  shell — Open interactive shell on device
# ------------------------------------------------------------

shell:
	@$(ADB) shell

# ------------------------------------------------------------
#  test — Run local unit tests
# ------------------------------------------------------------

test:
	$(ENSURE_SDK)
	@$(ECHO) ">>> $(YELLOW)Running unit tests...$(RESET)"
	@$(GRADLE) test
	@$(ECHO) ">>> $(GREEN)All unit tests passed.$(RESET)"

# ------------------------------------------------------------
#  lint — Run Android lint checks
# ------------------------------------------------------------

lint:
	$(ENSURE_SDK)
	@$(ECHO) ">>> $(YELLOW)Running Android lint checks...$(RESET)"
	@$(GRADLE) lint
	@$(ECHO) ">>> $(GREEN)Lint analysis completed.$(RESET)"

# ------------------------------------------------------------
#  clean — Clean Gradle build artifacts
# ------------------------------------------------------------

clean:
	$(ENSURE_SDK)
	@$(ECHO) ">>> $(YELLOW)Cleaning Gradle build cache & artifacts...$(RESET)"
	@$(GRADLE) clean
	@$(ECHO) ">>> $(CYAN)Clean completed.$(RESET)"

# ------------------------------------------------------------
#  fclean — Full purge (Gradle clean, remove screenshots, uninstall)
# ------------------------------------------------------------

fclean: clean
	@$(ECHO) ">>> $(RED)Performing full cleanup...$(RESET)"
	@$(RM) $(SCREENSHOT_DIR)
	-@$(ADB) uninstall $(PACKAGE) $(IGNORE)
	@$(ECHO) ">>> $(GREEN)Full clean completed.$(RESET)"

# ------------------------------------------------------------
#  check-sdk — Display project SDK config & environment SDKs
# ------------------------------------------------------------

check-sdk:
	@$(ECHO) ""
	@$(ECHO) " >>> $(CYAN)[1/3] Project Android SDK Configuration (app/build.gradle.kts):$(RESET)"
	@sed -n '7,15p;28,31p' app/build.gradle.kts
	@$(ECHO) ""
	@$(ECHO) " >>> $(CYAN)[2/3] Gradle Plugin & Dependencies (gradle/libs.versions.toml):$(RESET)"
	@head -n 25 gradle/libs.versions.toml
	@$(ECHO) ""
	@$(ECHO) " >>> $(CYAN)[3/3] Host Environment Android SDK & Java Versions:$(RESET)"
	@if [ -n "$$ANDROID_HOME" ] && [ -d "$$ANDROID_HOME/platforms" ]; then \
		$(ECHO) "   Installed SDK platforms in $$ANDROID_HOME/platforms/:"; \
		ls -1 "$$ANDROID_HOME/platforms" 2>/dev/null | sed 's/^/     - /'; \
	elif [ -n "$$ANDROID_SDK_ROOT" ] && [ -d "$$ANDROID_SDK_ROOT/platforms" ]; then \
		$(ECHO) "   Installed SDK platforms in $$ANDROID_SDK_ROOT/platforms/:"; \
		ls -1 "$$ANDROID_SDK_ROOT/platforms" 2>/dev/null | sed 's/^/     - /'; \
	elif [ -d "$(HOME)/Android/Sdk/platforms" ]; then \
		$(ECHO) "   Installed SDK platforms in $(HOME)/Android/Sdk/platforms/:"; \
		ls -1 "$(HOME)/Android/Sdk/platforms" 2>/dev/null | sed 's/^/     - /'; \
	elif [ -d "$(HOME)/goinfre/Android/Sdk/platforms" ]; then \
		$(ECHO) "   Installed SDK platforms in $(HOME)/goinfre/Android/Sdk/platforms/:"; \
		ls -1 "$(HOME)/goinfre/Android/Sdk/platforms" 2>/dev/null | sed 's/^/     - /'; \
	elif [ -d "/usr/lib/android-sdk/platforms" ]; then \
		$(ECHO) "   Installed SDK platforms in /usr/lib/android-sdk/platforms/:"; \
		ls -1 "/usr/lib/android-sdk/platforms" 2>/dev/null | sed 's/^/     - /'; \
	else \
		$(ECHO) "   No standard Android SDK platform directory found automatically."; \
		$(ECHO) "   Check ANDROID_HOME or Android Studio > SDK Manager."; \
	fi
	@if command -v java >/dev/null 2>&1; then \
		$(ECHO) "   Java Runtime:"; \
		java -version 2>&1 | sed 's/^/     /'; \
	fi
	@$(ECHO) ""

# ------------------------------------------------------------
#  check-cif — Display RCSB CIF endpoint and parser wiring
# ------------------------------------------------------------

check-cif:
	@$(ECHO) ""
	@$(ECHO) " >>> $(CYAN)[1/3] RCSB Base URL (app/.../data/network/NetworkClient.kt):$(RESET)"
	@sed -n '11p' app/src/main/java/com/example/swiftyproteins/data/network/NetworkClient.kt | sed 's/^[ \t]*/   /'
	@$(ECHO) ""
	@$(ECHO) " >>> $(CYAN)[2/3] CIF Endpoint Definition (app/.../data/network/RcsbApiService.kt):$(RESET)"
	@sed -n '9,10p' app/src/main/java/com/example/swiftyproteins/data/network/RcsbApiService.kt | sed 's/^[ \t]*/   /'
	@$(ECHO) ""
	@$(ECHO) " >>> $(CYAN)[3/3] CIF Parser & Loop Parsing (app/.../data/parser/CifParser.kt):$(RESET)"
	@grep -n -E "loop_|_chem_comp_atom|_chem_comp_bond|model_Cartn" app/src/main/java/com/example/swiftyproteins/data/parser/CifParser.kt | head -n 8 | sed 's/^[ \t]*/   /'
	@$(ECHO) ""

# ------------------------------------------------------------
#  re — Clean everything and rebuild
# ------------------------------------------------------------

re: fclean all

# ------------------------------------------------------------
#  help — Show available rules
# ------------------------------------------------------------

help:
	@$(ECHO) ""
	@$(ECHO) " $(CYAN)AVAILABLE RULES$(RESET)"
	@$(ECHO) ""
	@$(ECHO) "     $(CYAN)all$(RESET)              Build debug APK"
	@$(ECHO) "     $(CYAN)build$(RESET)            Compile debug APK ($(APK_DEBUG))"
	@$(ECHO) "     $(CYAN)release$(RESET)          Compile unsigned release APK"
	@$(ECHO) "     $(CYAN)install-usb$(RESET)      Build & install APK directly on USB-connected phone (-d)"
	@$(ECHO) "     $(CYAN)install-emu$(RESET)      Build & install APK on running emulator (-e)"
	@$(ECHO) "     $(CYAN)install$(RESET)          Build & install APK on default/active ADB target"
	@$(ECHO) "     $(CYAN)start-usb$(RESET)        Build, install, and launch app on USB phone"
	@$(ECHO) "     $(CYAN)start-emu$(RESET)        Build, install, and launch app on emulator"
	@$(ECHO) "     $(CYAN)start$(RESET)            Build, install, and launch app on active target"
	@$(ECHO) "     $(CYAN)run / run-usb$(RESET)    Cold-launch SplashActivity on device"
	@$(ECHO) "     $(CYAN)stop$(RESET)             Force-stop the app process"
	@$(ECHO) "     $(CYAN)restart$(RESET)          Stop and relaunch the app"
	@$(ECHO) "     $(CYAN)clear-data$(RESET)       Wipe app credentials & KeyStore (clean slate for defense)"
	@$(ECHO) "     $(CYAN)clear-data-usb$(RESET)   Wipe app credentials on USB phone"
	@$(ECHO) "     $(CYAN)uninstall$(RESET)        Completely uninstall package from active target"
	@$(ECHO) "     $(CYAN)uninstall-usb$(RESET)    Completely uninstall package from USB phone"
	@$(ECHO) "     $(CYAN)bio-success$(RESET)      Simulate valid biometric touch on emulator"
	@$(ECHO) "     $(CYAN)bio-fail$(RESET)         Simulate invalid biometric touch on emulator"
	@$(ECHO) "     $(CYAN)logs / logs-usb$(RESET)  Stream filtered logcat for SwiftyProteins"
	@$(ECHO) "     $(CYAN)logcat-clear$(RESET)     Clear logcat buffer on device"
	@$(ECHO) "     $(CYAN)devices$(RESET)          List connected USB devices and emulators"
	@$(ECHO) "     $(CYAN)screenshot$(RESET)       Capture device screen to $(SCREENSHOT_DIR)/"
	@$(ECHO) "     $(CYAN)pull-photos$(RESET)      Pull in-app exported protein snapshots from device"
	@$(ECHO) "     $(CYAN)shell$(RESET)            Open interactive ADB shell on device"
	@$(ECHO) "     $(CYAN)test$(RESET)             Run unit tests"
	@$(ECHO) "     $(CYAN)lint$(RESET)             Run Android lint checks"
	@$(ECHO) "     $(CYAN)check-sdk$(RESET)        Display project SDK config & machine SDK versions"
	@$(ECHO) "     $(CYAN)check-cif$(RESET)        Display RCSB .cif endpoint and parser implementation"
	@$(ECHO) "     $(CYAN)clean$(RESET)            Clean Gradle build artifacts"
	@$(ECHO) "     $(CYAN)fclean$(RESET)           Full reset: Gradle clean, remove screenshots, uninstall"
	@$(ECHO) "     $(CYAN)re$(RESET)               Clean everything and rebuild"
	@$(ECHO) "     $(CYAN)help$(RESET)             Show available rules"
	@$(ECHO) ""
