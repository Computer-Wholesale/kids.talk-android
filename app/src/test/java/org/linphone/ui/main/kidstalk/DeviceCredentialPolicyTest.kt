package org.linphone.ui.main.kidstalk

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceCredentialPolicyTest {
    @Test
    fun `uses the legacy credential intent on protected API 28 and 29 devices`() {
        assertEquals(
            DeviceCredentialAction.LegacyDeviceCredential,
            DeviceCredentialPolicy.resolve(apiLevel = 28, isDeviceSecure = true, biometricAvailable = true)
        )
        assertEquals(
            DeviceCredentialAction.LegacyDeviceCredential,
            DeviceCredentialPolicy.resolve(apiLevel = 29, isDeviceSecure = true, biometricAvailable = false)
        )
    }

    @Test
    fun `uses biometric prompt with device credential fallback on protected API 30 plus devices`() {
        assertEquals(
            DeviceCredentialAction.BiometricWithDeviceCredentialFallback,
            DeviceCredentialPolicy.resolve(apiLevel = 30, isDeviceSecure = true, biometricAvailable = true)
        )
        assertEquals(
            DeviceCredentialAction.LegacyDeviceCredential,
            DeviceCredentialPolicy.resolve(apiLevel = 34, isDeviceSecure = true, biometricAvailable = false)
        )
    }

    @Test
    fun `proceeds without a fabricated gate when no device credential exists`() {
        assertEquals(
            DeviceCredentialAction.ProceedWithoutCredential,
            DeviceCredentialPolicy.resolve(apiLevel = 34, isDeviceSecure = false, biometricAvailable = true)
        )
    }
}
