# Feature: Password Security & Storage

## Overview
Guarantees cryptographic protection of user credentials at rest, ensuring that passwords and sensitive authentication data are never exposed or stored in plain text.

---

## Evaluation Requirements
* **No Plaintext Passwords**: Verify that passwords are never stored in plain text under any circumstances.
* **Platform Secure Storage**: Utilization of platform-specific hardware-backed secure storage (Android KeyStore / Keychain on iOS).
* **Cryptographic Hashing**: Application of industry-standard cryptographic hashing algorithms (SHA-256 / bcrypt) prior to storage.

---

## Technical Specifications
* **Key Management**:
  * Android KeyStore Provider: Stores cryptographic master keys in hardware-backed secure elements (TEE/StrongBox when available).
  * `MasterKey.Builder` with `AES256_GCM` scheme.
* **Encrypted Storage**:
  * `androidx.security.crypto:security-crypto` library.
  * `EncryptedSharedPreferences`:
    * Key Encryption: `AES256_SIV`
    * Value Encryption: `AES256_GCM`
* **Password Hashing**:
  * Algorithm: SHA-256 (`MessageDigest.getInstance("SHA-256")`).
  * Process: User password string $\rightarrow$ UTF-8 bytes $\rightarrow$ SHA-256 digest $\rightarrow$ Hex string representation $\rightarrow$ stored into encrypted preferences.
  * Validation: Input password is independently hashed and compared against the stored hash digest.

---

## Security Architecture
```
[ User Input Password ]
          │
          ▼
   [ SHA-256 Hashing ]
          │
          ▼
 [ 64-char Hex Digest ]
          │
          ▼
[ Android Keystore MasterKey (AES256_GCM) ]
          │
          ▼
[ EncryptedSharedPreferences (AES256_SIV / AES256_GCM) ]
          │
          ▼
  [ Encrypted File on Disk: /data/data/.../secure_user_prefs.xml ]
```

---

## Verification & Testing
1. Code Inspection: Confirm `SecureStorage.kt` hashes passwords via `MessageDigest` before any disk write.
2. Device Inspection: Inspect `/data/data/com.example.swiftyproteins/shared_prefs/secure_user_prefs.xml` via ADB root/backup; verify keys and values are completely encrypted ciphertext.
