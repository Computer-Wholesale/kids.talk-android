package org.linphone.ui.main.kidstalk

import android.content.Intent
import android.graphics.Rect
import android.os.ParcelFileDescriptor
import android.view.View
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
    fun setupControlsAreReachableAndUnclippedAtConfiguredHostileScale() {
        // I-10: the API-35 workflow supplies font_scale=1.30 and density=560.
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", false)
        ).use { scenario ->
            assertHostileScale(scenario)
            listOf(
                org.linphone.R.id.setup_name_label,
                org.linphone.R.id.setup_name_input,
                org.linphone.R.id.setup_number_label,
                org.linphone.R.id.setup_number_input,
                org.linphone.R.id.save_contact_button
            ).forEach { id ->
                onView(withId(id)).perform(scrollTo()).check(matches(isDisplayed()))
                assertFullyVisibleWithinContent(scenario, id)
            }
            scenario.onActivity { activity ->
                assertFalse(activity.findViewById<View>(org.linphone.R.id.save_contact_button).isEnabled)
            }
            captureScaleEvidence("setup-state")
        }
    }

    @Test
    fun callControlsAreReachableAndUnclippedAtConfiguredHostileScale() {
        // I-11: the API-35 workflow supplies font_scale=1.30 and density=560.
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", true)
        ).use { scenario ->
            assertHostileScale(scenario)
            listOf(
                org.linphone.R.id.contact_name_label,
                org.linphone.R.id.call_button,
                org.linphone.R.id.change_contact_button
            ).forEach { id ->
                onView(withId(id)).perform(scrollTo()).check(matches(isDisplayed()))
                assertFullyVisibleWithinContent(scenario, id)
            }
            onView(withId(org.linphone.R.id.call_button)).check(matches(isEnabled()))
            onView(withId(org.linphone.R.id.change_contact_button)).check(matches(isEnabled()))
            captureScaleEvidence("call-state")
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
            val root = activity.findViewById<View>(android.R.id.content)
            val view = activity.findViewById<View>(id)
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
            assertTrue("I-10/I-11 scroll container must remain shown", scrollContainer.visibility == View.VISIBLE)
        }
    }
}
