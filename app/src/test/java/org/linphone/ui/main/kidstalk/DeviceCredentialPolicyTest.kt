package org.linphone.ui.main.kidstalk

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceCredentialPolicyTest {
    @Test
    fun `secure API 28 device uses the platform credential intent`() {
        assertEquals(
            DeviceCredentialAction.LegacyDeviceCredential,
            DeviceCredentialPolicy.resolve(
                apiLevel = 28,
                isDeviceSecure = true,
                combinedCapability = CombinedAuthenticatorCapability.Available
            )
        )
    }

    @Test
    fun `secure API 29 device uses the platform credential intent`() {
        assertEquals(
            DeviceCredentialAction.LegacyDeviceCredential,
            DeviceCredentialPolicy.resolve(
                apiLevel = 29,
                isDeviceSecure = true,
                combinedCapability = CombinedAuthenticatorCapability.Available
            )
        )
    }

    @Test
    fun `secure API 30 plus device uses combined BiometricPrompt whenever combined capability is available`() {
        assertEquals(
            DeviceCredentialAction.CombinedBiometricPrompt,
            DeviceCredentialPolicy.resolve(
                apiLevel = 34,
                isDeviceSecure = true,
                combinedCapability = CombinedAuthenticatorCapability.Available
            )
        )
    }

    @Test
    fun `secure API 30 plus device does not require biometric-only availability for combined prompt`() {
        assertEquals(
            DeviceCredentialAction.CombinedBiometricPrompt,
            DeviceCredentialPolicy.resolve(
                apiLevel = 30,
                isDeviceSecure = true,
                combinedCapability = CombinedAuthenticatorCapability.Available
            )
        )
    }

    @Test
    fun `unavailable combined capability on API 30 plus uses the explicit exceptional fallback`() {
        assertEquals(
            DeviceCredentialAction.ExceptionalLegacyDeviceCredential,
            DeviceCredentialPolicy.resolve(
                apiLevel = 34,
                isDeviceSecure = true,
                combinedCapability = CombinedAuthenticatorCapability.UnknownError
            )
        )
    }

    @Test
    fun `insecure device does not request authentication`() {
        assertEquals(
            DeviceCredentialAction.ProceedWithoutCredential,
            DeviceCredentialPolicy.resolve(
                apiLevel = 34,
                isDeviceSecure = false,
                combinedCapability = CombinedAuthenticatorCapability.Available
            )
        )
    }
}
