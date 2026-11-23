package com.example.swiftyproteins.ui.auth

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.swiftyproteins.SwiftyProteinsApp
import com.example.swiftyproteins.databinding.ActivityLoginBinding
import com.example.swiftyproteins.ui.protein.ProteinListActivity
import com.example.swiftyproteins.util.SecureStorage

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var secureStorage: SecureStorage
    private var isRegisterMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)

        // If no user exists yet, start in Register mode; otherwise start in Login mode
        isRegisterMode = !secureStorage.hasUser()
        updateUiMode()

        // Autofill last logged in user if available
        secureStorage.getLastUser()?.let { lastUser ->
            if (!isRegisterMode) {
                binding.etUsername.setText(lastUser)
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (isRegisterMode) {
                handleRegistration()
            } else {
                handleLogin()
            }
        }

        binding.btnToggleMode.setOnClickListener {
            isRegisterMode = !isRegisterMode
            updateUiMode()
        }

        binding.btnBiometric.setOnClickListener {
            triggerBiometricAuthentication(autoPrompt = false)
        }

        // Auto-prompt biometrics on launch if registered and biometrics is ready
        if (!isRegisterMode && secureStorage.hasUser()) {
            checkAndAutoPromptBiometrics()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUiMode()
    }

    private fun isBiometricHardwareSupported(): Boolean {
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS ||
                canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    private fun updateUiMode() {
        if (isRegisterMode) {
            binding.tvSubtitle.text = "Create a new account"
            binding.tilConfirmPassword.visibility = View.VISIBLE
            binding.btnSubmit.text = "Create Account"
            binding.btnToggleMode.text = "Already have an account? Log In"
            binding.layoutBiometricDivider.visibility = View.GONE
            binding.btnBiometric.visibility = View.GONE
        } else {
            binding.tvSubtitle.text = "Log in to continue"
            binding.tilConfirmPassword.visibility = View.GONE
            binding.btnSubmit.text = "Log In"
            binding.btnToggleMode.text = "Don't have an account? Register"
            val showBiometricOption = isBiometricHardwareSupported()
            binding.layoutBiometricDivider.visibility = if (showBiometricOption) View.VISIBLE else View.GONE
            binding.btnBiometric.visibility = if (showBiometricOption) View.VISIBLE else View.GONE
        }
    }

    private fun handleRegistration() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showErrorDialog("Missing Fields", "Please fill in all fields to register.")
            return
        }

        if (password.length < 4) {
            showErrorDialog("Weak Password", "Password must be at least 4 characters long.")
            return
        }

        if (password != confirmPassword) {
            showErrorDialog("Password Mismatch", "Passwords do not match. Please re-enter.")
            return
        }

        if (secureStorage.isUserRegistered(username)) {
            AlertDialog.Builder(this)
                .setTitle("Account Already Exists")
                .setMessage("An account with username '$username' is already registered.\n\nWould you like to log in instead?")
                .setPositiveButton("Log In") { _, _ ->
                    isRegisterMode = false
                    updateUiMode()
                }
                .setNegativeButton("Choose Different Username", null)
                .show()
            return
        }

        val success = secureStorage.registerUser(username, password)
        if (success) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
            proceedToApp()
        } else {
            showErrorDialog("Registration Failed", "Could not create account. Please try again.")
        }
    }

    private fun handleLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showErrorDialog("Missing Credentials", "Please enter both username and password.")
            return
        }

        if (!secureStorage.isUserRegistered(username)) {
            showErrorDialog("Account Not Found", "No account registered with username '$username'.\n\nPlease check the spelling or register first.")
            return
        }

        if (secureStorage.validateCredentials(username, password)) {
            proceedToApp()
        } else {
            showErrorDialog("Authentication Failed", "Incorrect password. Please try again.")
        }
    }

    private fun checkAndAutoPromptBiometrics() {
        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            triggerBiometricAuthentication(autoPrompt = true)
        }
    }

    private fun triggerBiometricAuthentication(autoPrompt: Boolean) {
        if (!secureStorage.hasUser()) {
            if (!autoPrompt) {
                showErrorDialog("No Account Registered", "Please create an account first before using biometric login.")
            }
            return
        }

        val biometricManager = BiometricManager.from(this)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

        when (canAuth) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                if (!autoPrompt) {
                    AlertDialog.Builder(this)
                        .setTitle("No Biometrics Enrolled")
                        .setMessage("No fingerprint or face credential has been set up on this device.\n\nPlease enroll a fingerprint in Android Settings, or use your password to log in.")
                        .setPositiveButton("Open Settings") { _, _ ->
                            try {
                                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                            } catch (e: Exception) {
                                startActivity(Intent(Settings.ACTION_SETTINGS))
                            }
                        }
                        .setNegativeButton("Use Password", null)
                        .show()
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                if (!autoPrompt) {
                    showErrorDialog("Not Supported", "Biometric hardware is not available on this device. Please log in using your password.")
                }
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                if (!autoPrompt) {
                    showErrorDialog("Hardware Unavailable", "Biometric sensor is currently unavailable. Please log in using your password.")
                }
            }
            else -> {
                if (!autoPrompt) {
                    showErrorDialog("Biometrics Unavailable", "Biometric authentication is not ready. Please use password login.")
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    proceedToApp()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user manually pressed cancel / negative button, do not show error dialog
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        showErrorDialog("Biometric Error", "$errString")
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    AlertDialog.Builder(this@LoginActivity)
                        .setTitle("Authentication Failed")
                        .setMessage("Biometric not recognized. Please try again or use your password.")
                        .setPositiveButton("Retry") { _, _ ->
                            showBiometricPrompt()
                        }
                        .setNegativeButton("Use Password", null)
                        .show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Confirm your fingerprint or face to log in")
            .setNegativeButtonText("Use Password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showErrorDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun proceedToApp() {
        SwiftyProteinsApp.isSessionLocked = false
        startActivity(Intent(this, ProteinListActivity::class.java))
        finish()
    }
}
