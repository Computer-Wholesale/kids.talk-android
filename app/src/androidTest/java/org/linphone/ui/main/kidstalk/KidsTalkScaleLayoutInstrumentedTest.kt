package org.linphone.ui.main.kidstalk

import android.content.Intent
import android.graphics.Rect
import android.os.ParcelFileDescriptor
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KidsTalkScaleLayoutInstrumentedTest {
    @Test
    fun setupControlsAreReachableAndLegibleAtConfiguredHostileScale() {
        // I-10: the API-35 workflow supplies font_scale=1.30 and an explicit phone-class profile.
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", false)
        ).use { scenario ->
            assertHostileScale(scenario)
            awaitCoherentCheckpoint(scenario)
            assertPrimaryCheckpoint(
                scenario,
                listOf(
                org.linphone.R.id.setup_name_label,
                org.linphone.R.id.setup_name_input,
                org.linphone.R.id.setup_number_label,
                org.linphone.R.id.setup_number_input,
                org.linphone.R.id.save_contact_button
            )
            )
            assertRenderedText(scenario, org.linphone.R.id.setup_name_label, org.linphone.R.string.kt_contact_name_label)
            assertRenderedText(scenario, org.linphone.R.id.setup_number_label, org.linphone.R.string.kt_contact_extension_label)
            assertRenderedText(scenario, org.linphone.R.id.save_contact_button, org.linphone.R.string.kt_save_contact)
            scenario.onActivity { activity ->
                assertFalse(activity.findViewById<TextView>(org.linphone.R.id.save_contact_button).isEnabled)
            }
            captureScaleEvidence("setup-primary-state")
            assertReachability(
                scenario,
                listOf(
                org.linphone.R.id.setup_name_label,
                org.linphone.R.id.setup_name_input,
                org.linphone.R.id.setup_number_label,
                org.linphone.R.id.setup_number_input,
                org.linphone.R.id.save_contact_button
            )
            )
        }
    }

    @Test
    fun callControlsAreReachableAndLegibleAtConfiguredHostileScale() {
        // I-11: the API-35 workflow supplies font_scale=1.30 and an explicit phone-class profile.
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", true)
        ).use { scenario ->
            assertHostileScale(scenario)
            awaitCoherentCheckpoint(scenario)
            assertPrimaryCheckpoint(
                scenario,
                listOf(
                org.linphone.R.id.contact_name_label,
                org.linphone.R.id.call_button
            )
            )
            assertRenderedText(scenario, org.linphone.R.id.contact_name_label, "Household contact")
            assertRenderedText(scenario, org.linphone.R.id.call_button, org.linphone.R.string.kt_call_button)
            onView(withId(org.linphone.R.id.call_button)).check(matches(isEnabled()))
            captureScaleEvidence("call-primary-state")
            assertReachability(
                scenario,
                listOf(
                org.linphone.R.id.contact_name_label,
                org.linphone.R.id.call_button,
                org.linphone.R.id.change_contact_button
            )
            )
            onView(withId(org.linphone.R.id.change_contact_button)).check(matches(isEnabled()))
        }
    }

    private fun awaitCoherentCheckpoint(scenario: ActivityScenario<KidsTalkFragmentTestHostActivity>) {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        scenario.onActivity { activity ->
            val scrollContainer = activity.findViewById<NestedScrollView>(org.linphone.R.id.kids_talk_scroll_container)
            scrollContainer.scrollTo(0, 0)
            scrollContainer.requestLayout()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun assertPrimaryCheckpoint(
        scenario: ActivityScenario<KidsTalkFragmentTestHostActivity>,
        ids: List<Int>
    ) {
        ids.forEach { id ->
            onView(withId(id)).check(matches(isDisplayed()))
            assertFullyVisibleWithinContent(scenario, id)
        }
    }

    private fun assertReachability(
        scenario: ActivityScenario<KidsTalkFragmentTestHostActivity>,
        ids: List<Int>
    ) {
        ids.forEach { id ->
            onView(withId(id)).perform(scrollTo()).check(matches(isDisplayed()))
            assertFullyVisibleWithinContent(scenario, id)
        }
    }

    private fun assertRenderedText(
        scenario: ActivityScenario<KidsTalkFragmentTestHostActivity>,
        id: Int,
        expectedStringResource: Int
    ) {
        scenario.onActivity { activity ->
            assertRenderedText(
                activity.findViewById(id),
                activity.getString(expectedStringResource),
                id
            )
        }
    }

    private fun assertRenderedText(
        scenario: ActivityScenario<KidsTalkFragmentTestHostActivity>,
        id: Int,
        expected: String
    ) {
        scenario.onActivity { activity ->
            assertRenderedText(activity.findViewById(id), expected, id)
        }
    }

    private fun assertRenderedText(view: TextView, expected: String, id: Int) {
        assertEquals("I-10/I-11 primary text $id must equal its configured value", expected, view.text.toString())
        assertTrue("I-10/I-11 primary text $id must have non-zero width", view.width > 0)
        assertTrue("I-10/I-11 primary text $id must have non-zero height", view.height > 0)
        assertFalse("I-10/I-11 primary text $id layout must be settled", view.isLayoutRequested)
        val layout = view.layout
        assertNotNull("I-10/I-11 primary text $id must have a rendered layout", layout)
        val contentWidth = view.width - view.compoundPaddingLeft - view.compoundPaddingRight
        assertTrue("I-10/I-11 primary text $id must have positive content width", contentWidth > 0)
        layout?.let { renderedLayout ->
            for (line in 0 until renderedLayout.lineCount) {
                assertEquals(
                    "I-10/I-11 primary text $id must not ellipsize line $line",
                    0,
                    renderedLayout.getEllipsisCount(line)
                )
                assertTrue(
                    "I-10/I-11 primary text $id line $line must fit its content width",
                    renderedLayout.getLineWidth(line) <= contentWidth.toFloat() + 0.5f
                )
            }
        }
    }

    private fun captureScaleEvidence(state: String) {
        val uiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        ParcelFileDescriptor.AutoCloseInputStream(
            uiAutomation.executeShellCommand("mkdir -p /sdcard/kid394-scale-evidence")
        ).close()
        ParcelFileDescriptor.AutoCloseInputStream(
            uiAutomation.executeShellCommand("screencap -p /sdcard/kid394-scale-evidence/$state.png")
        ).close()
        val screenshot = uiAutomation.takeScreenshot()
        assertNotNull("I-10/I-11 $state screenshot must be captured", screenshot)
        screenshot?.recycle()
    }

    private fun assertHostileScale(scenario: ActivityScenario<KidsTalkFragmentTestHostActivity>) {
        scenario.onActivity { activity ->
            assertEquals(1.30f, activity.resources.configuration.fontScale, 0.01f)
            assertEquals(560, activity.resources.displayMetrics.densityDpi)
        }
    }

    private fun assertFullyVisibleWithinContent(
        scenario: ActivityScenario<KidsTalkFragmentTestHostActivity>,
        id: Int
    ) {
        scenario.onActivity { activity ->
            val scrollContainer = activity.findViewById<NestedScrollView>(org.linphone.R.id.kids_talk_scroll_container)
            val root = activity.findViewById<android.view.View>(android.R.id.content)
            val view = activity.findViewById<android.view.View>(id)
            val rootBounds = Rect().also(root::getGlobalVisibleRect)
            val visibleBounds = Rect()
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val unobscuredBounds = Rect(
                location[0],
                location[1],
                location[0] + view.width,
                location[1] + view.height
            )

            assertTrue("I-10/I-11 control $id must have a visible region", view.getGlobalVisibleRect(visibleBounds))
            assertEquals("I-10/I-11 control $id must not be clipped", unobscuredBounds, visibleBounds)
            assertTrue("I-10/I-11 control $id must fit horizontally", rootBounds.contains(visibleBounds.left, visibleBounds.top))
            assertTrue("I-10/I-11 control $id must fit vertically", rootBounds.contains(visibleBounds.right - 1, visibleBounds.bottom - 1))
            assertTrue("I-10/I-11 scroll container must remain shown", scrollContainer.visibility == android.view.View.VISIBLE)
        }
    }
}
