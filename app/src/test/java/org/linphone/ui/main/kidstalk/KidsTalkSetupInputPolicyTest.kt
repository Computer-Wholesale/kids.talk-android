package org.linphone.ui.main.kidstalk

import kotlin.test.Test
import kotlin.test.assertEquals

class KidsTalkSetupInputPolicyTest {
    @Test
    fun `six-digit extension not beginning with six or seven is valid during live validation`() {
        assertEquals(
            SetupUsernameValidation.Valid,
            KidsTalkSetupInputPolicy.validateUsername("512345")
        )
    }

    @Test
    fun `non-digit input is identified before extension-length feedback`() {
        assertEquals(
            SetupUsernameValidation.NonDigit,
            KidsTalkSetupInputPolicy.validateUsername("12a456")
        )
    }

    @Test
    fun `non-six-digit numeric input is rejected by the shared contact policy`() {
        assertEquals(
            SetupUsernameValidation.InvalidExtension,
            KidsTalkSetupInputPolicy.validateUsername("51234")
        )
    }

    @Test
    fun `empty input has no visible error`() {
        assertEquals(
            SetupUsernameValidation.Empty,
            KidsTalkSetupInputPolicy.validateUsername("")
        )
    }
}
