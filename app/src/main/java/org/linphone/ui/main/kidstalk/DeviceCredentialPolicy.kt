package org.linphone.ui.main.kidstalk

/**
 * Testable platform-independent decision for a one-time local contact-change request.
 *
 * The fragment performs the Android API calls selected here. This policy does not grant
 * access itself and no result is retained beyond the single requested contact change.
 */
enum class DeviceCredentialAction {
    LegacyDeviceCredential,
    BiometricWithDeviceCredentialFallback,
    ProceedWithoutCredential
}

object DeviceCredentialPolicy {
    fun resolve(
        apiLevel: Int,
        isDeviceSecure: Boolean,
        biometricAvailable: Boolean
    ): DeviceCredentialAction {
        if (!isDeviceSecure) return DeviceCredentialAction.ProceedWithoutCredential
        if (apiLevel <= 29) return DeviceCredentialAction.LegacyDeviceCredential
        return if (biometricAvailable) {
            DeviceCredentialAction.BiometricWithDeviceCredentialFallback
        } else {
            DeviceCredentialAction.LegacyDeviceCredential
        }
    }
}
