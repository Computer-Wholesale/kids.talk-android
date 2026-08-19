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
 * API 28–29 use the platform device-credential intent. API 30+ asks Android whether the
 * combined BIOMETRIC_STRONG | DEVICE_CREDENTIAL capability is available and, when it is,
 * presents BiometricPrompt with those same combined authenticators. No authorization state,
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
        val action = DeviceCredentialPolicy.resolve(
            apiLevel = Build.VERSION.SDK_INT,
            isDeviceSecure = keyguardManager.isDeviceSecure,
            combinedCapability = combinedCapability(context)
        )

        when (action) {
            DeviceCredentialAction.CombinedBiometricPrompt -> requestCombinedPrompt(context)
            DeviceCredentialAction.LegacyDeviceCredential,
            DeviceCredentialAction.ExceptionalLegacyDeviceCredential -> {
                requestDeviceCredential(keyguardManager, context)
            }
            DeviceCredentialAction.ProceedWithoutCredential -> {
                onResult(DeviceCredentialGateResult.NoDeviceCredential)
            }
        }
    }

    private fun combinedCapability(context: Context): CombinedAuthenticatorCapability {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return CombinedAuthenticatorCapability.Unsupported
        }

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return when (BiometricManager.from(context).canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> CombinedAuthenticatorCapability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> CombinedAuthenticatorCapability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> CombinedAuthenticatorCapability.NoneEnrolled
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> CombinedAuthenticatorCapability.Unsupported
            else -> CombinedAuthenticatorCapability.UnknownError
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

    private fun requestCombinedPrompt(context: Context) {
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
                    onResult(DeviceCredentialGateResult.Cancelled)
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
