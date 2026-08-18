package org.linphone.ui.main.kidstalk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KidsTalkContactPolicyTest {
    @Test
    fun `accepts an exactly six-digit household extension`() {
        assertTrue(KidsTalkContactPolicy.isValidExtension("412978"))
        assertTrue(KidsTalkContactPolicy.isValidExtension("611978"))
    }

    @Test
    fun `rejects extensions outside the PBX six-digit boundary`() {
        assertFalse(KidsTalkContactPolicy.isValidExtension("12345"))
        assertFalse(KidsTalkContactPolicy.isValidExtension("1234567"))
    }

    @Test
    fun `rejects non-numeric extension values`() {
        assertFalse(KidsTalkContactPolicy.isValidExtension("12345a"))
        assertFalse(KidsTalkContactPolicy.isValidExtension("12 456"))
        assertFalse(KidsTalkContactPolicy.isValidExtension("+12345"))
    }
}
