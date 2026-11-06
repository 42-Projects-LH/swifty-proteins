# Feature: Biometric & Password Authentication

## Overview
Provides secure user authentication supporting dual modalities: native biometric verification (fingerprint / Face Unlock via Android `BiometricPrompt`) and username/password credentials.

---

## Evaluation Requirements
* **Account Creation & Login**: Users must be able to create an account and log in.
* **Biometric Authentication**: Verify that users can authenticate using native biometrics (`BiometricPrompt` on Android).
* **Biometric Authentication Failure Alert**: If biometric authentication fails (unrecognized credential, sensor error, or lockout), a **popup/alert dialog must inform the user** of the failure.
* **Fallback for Unsupported Devices**: If the device does not have biometric hardware or credentials enrolled, users must be able to log in using a username and password.

---

## Technical Specifications
* **Architecture**: Android `androidx.biometric:biometric` library.
* **Components**:
  * `BiometricManager`: Queries hardware capabilities and enrollment state via `canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)`.
  * `BiometricPrompt`: System-managed cryptographic authentication dialog.
  * `AuthenticationCallback`: Handles `onAuthenticationSucceeded`, `onAuthenticationError`, and `onAuthenticationFailed`.
* **UI Modes**:
  * **Register Mode**: Username, Password, Confirm Password, Create Account action.
  * **Login Mode**: Username, Password, Log In action, Log In with Biometrics action.
* **Failure Handling**:
  * On failure/rejection: Modal `AlertDialog` displaying explicit error context with options to retry or use password.

---

## User Flow
1. **First-time User**: Launches app in Register mode, inputs credentials, and creates account.
2. **Returning User (Biometric)**: Taps "Log In with Biometrics" (or triggered on launch), touches fingerprint sensor $\rightarrow$ proceeds to Protein List.
3. **Biometric Failure**: Touches sensor with unrecognized finger $\rightarrow$ Alert popup appears informing user of failure $\rightarrow$ user can retry or switch to password.
4. **Fallback User**: Enters username and password $\rightarrow$ taps "Log In" $\rightarrow$ credentials validated $\rightarrow$ proceeds to Protein List.

---

## Verification & Testing
1. Test account registration: Register a new user and confirm credentials persist.
2. Test biometric success: Trigger biometric login and authenticate with registered finger.
3. Test biometric failure: Present invalid finger or cancel prompt; verify the failure alert popup is displayed.
4. Test password fallback: Log in using username and password without touching the biometric prompt.
