package org.linphone.ui.main.kidstalk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KidsTalkExternalIntentPolicyTest {
    @Test
    fun `contains all generic and legacy external call inputs`() {
        val hostileInputs = listOf(
            "tel",
            "callto",
            "sip-linphone",
            "linphone-sip",
            "sip",
            "sips"
        )

        hostileInputs.forEach { scheme ->
            assertTrue(
                "Expected $scheme to remain contained",
                KidsTalkExternalIntentPolicy.containsExternalInput("android.intent.action.VIEW", scheme)
            )
        }
        assertTrue(KidsTalkExternalIntentPolicy.containsExternalInput("android.intent.action.VIEW", null))
    }

    @Test
    fun `accepts only the approved KidsTalk provisioning exception`() {
        assertTrue(
            KidsTalkExternalIntentPolicy.acceptsProvisioning(
                "android.intent.action.VIEW",
                "kidstalk-config"
            )
        )
        assertFalse(
            KidsTalkExternalIntentPolicy.containsExternalInput(
                "android.intent.action.VIEW",
                "kidstalk-config"
            )
        )
    }

    @Test
    fun `does not treat the main activity entry as external input`() {
        assertFalse(KidsTalkExternalIntentPolicy.containsExternalInput("android.intent.action.MAIN", null))
        assertFalse(KidsTalkExternalIntentPolicy.containsExternalInput(null, null))
    }
}
