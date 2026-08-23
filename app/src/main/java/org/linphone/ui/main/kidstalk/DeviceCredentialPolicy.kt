package org.linphone.ui.main.kidstalk

/**
 * Result of querying the API 30+ combined BIOMETRIC_STRONG | DEVICE_CREDENTIAL capability.
 *
 * The gate deliberately keeps the platform result explicit instead of reducing it to a
 * biometric-only boolean. A secure handset without an enrolled biometric can still satisfy
 * the combined capability through its device credential.
 */
enum class CombinedAuthenticatorCapability {
    Available,
    NoHardware,
    NoneEnrolled,
    Unsupported,
    UnknownError
}

/**
 * Testable platform-independent decision for a one-time local contact-change request.
 *
 * The fragment performs the Android API calls selected here. This policy does not grant
 * access itself and no result is retained beyond the single requested contact change.
 */
enum class DeviceCredentialAction {
    LegacyDeviceCredential,
    CombinedBiometricPrompt,
    ExceptionalLegacyDeviceCredential,
    ProceedWithoutCredential
}

object DeviceCredentialPolicy {
    fun resolve(
        apiLevel: Int,
        isDeviceSecure: Boolean,
        combinedCapability: CombinedAuthenticatorCapability
    ): DeviceCredentialAction {
        if (!isDeviceSecure) return DeviceCredentialAction.ProceedWithoutCredential
        if (apiLevel <= 29) return DeviceCredentialAction.LegacyDeviceCredential

        return when (combinedCapability) {
            CombinedAuthenticatorCapability.Available -> DeviceCredentialAction.CombinedBiometricPrompt
            CombinedAuthenticatorCapability.NoHardware,
            CombinedAuthenticatorCapability.NoneEnrolled,
            CombinedAuthenticatorCapability.Unsupported,
            CombinedAuthenticatorCapability.UnknownError -> DeviceCredentialAction.ExceptionalLegacyDeviceCredential
        }
    }
}
