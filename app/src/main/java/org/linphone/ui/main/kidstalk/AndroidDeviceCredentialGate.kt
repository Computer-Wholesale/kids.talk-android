package org.linphone.ui.main.kidstalk

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.linphone.R

sealed interface DeviceCredentialGateResult {
    data object Authorized : DeviceCredentialGateResult

    data object NoDeviceCredential : DeviceCredentialGateResult

    data object Cancelled : DeviceCredentialGateResult
}

/**
 * One-time handset-authentication gate for changing a locally stored household contact.
 *
 * The result is delivered only to the active Fragment instance. No authorization state,
 * biometric result, or device-credential result is persisted.
 */
class AndroidDeviceCredentialGate(
    private val fragment: Fragment,
    private val onResult: (DeviceCredentialGateResult) -> Unit
) {
    private val credentialLauncher: ActivityResultLauncher<Intent> =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onResult(
                if (result.resultCode == Activity.RESULT_OK) {
                    DeviceCredentialGateResult.Authorized
                } else {
                    DeviceCredentialGateResult.Cancelled
                }
            )
        }

    fun requestAuthorization() {
        val context = fragment.requireContext()
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val biometricAvailable = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS

        when (
            DeviceCredentialPolicy.resolve(
                apiLevel = Build.VERSION.SDK_INT,
                isDeviceSecure = keyguardManager.isDeviceSecure,
                biometricAvailable = biometricAvailable
            )
        ) {
            DeviceCredentialAction.BiometricWithDeviceCredentialFallback -> requestBiometric(context)
            DeviceCredentialAction.LegacyDeviceCredential -> requestDeviceCredential(keyguardManager, context)
            DeviceCredentialAction.ProceedWithoutCredential -> onResult(DeviceCredentialGateResult.NoDeviceCredential)
        }
    }

    private fun requestDeviceCredential(keyguardManager: KeyguardManager, context: Context) {
        val intent = keyguardManager.createConfirmDeviceCredentialIntent(
            context.getString(R.string.kt_contact_change_auth_title),
            context.getString(R.string.kt_contact_change_auth_subtitle)
        )
        if (intent == null) {
            onResult(DeviceCredentialGateResult.NoDeviceCredential)
        } else {
            credentialLauncher.launch(intent)
        }
    }

    private fun requestBiometric(context: Context) {
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            fragment,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(DeviceCredentialGateResult.Authorized)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (
                        errorCode == BiometricPrompt.ERROR_LOCKOUT ||
                        errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
                    ) {
                        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        requestDeviceCredential(keyguardManager, context)
                    } else {
                        onResult(DeviceCredentialGateResult.Cancelled)
                    }
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.kt_contact_change_auth_title))
            .setSubtitle(context.getString(R.string.kt_contact_change_auth_subtitle))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(promptInfo)
    }
}
