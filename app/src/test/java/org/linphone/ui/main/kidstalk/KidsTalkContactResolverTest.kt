package org.linphone.ui.main.kidstalk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KidsTalkContactResolverTest {
    private val local = KidsTalkContact(name = "Household contact", extension = "412978")
    private val managed = KidsTalkContact(name = "Managed contact", extension = "611978")

    @Test
    fun `managed contact overrides a retained local contact`() {
        val result = KidsTalkContactResolver.resolve(managed = managed, local = local)

        assertEquals(managed, result?.contact)
        assertTrue(result?.isManaged == true)
    }

    @Test
    fun `managed removal reverts to retained local contact`() {
        val result = KidsTalkContactResolver.resolve(managed = null, local = local)

        assertEquals(local, result?.contact)
        assertFalse(result?.isManaged ?: true)
    }

    @Test
    fun `no managed or local contact resolves to setup state`() {
        assertNull(KidsTalkContactResolver.resolve(managed = null, local = null))
    }
}
