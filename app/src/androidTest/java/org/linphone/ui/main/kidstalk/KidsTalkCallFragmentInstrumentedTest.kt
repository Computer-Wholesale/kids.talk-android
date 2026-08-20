package org.linphone.ui.main.kidstalk

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KidsTalkCallFragmentInstrumentedTest {
    @Test
    fun noContactShowsLabeledSetupAndDisabledSave() {
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", false)
        ).use {
            onView(withId(org.linphone.R.id.setup_view)).check(matches(isDisplayed()))
            onView(withId(org.linphone.R.id.setup_name_label)).check(matches(withText(org.linphone.R.string.kt_contact_name_label)))
            onView(withId(org.linphone.R.id.setup_number_label)).check(matches(withText(org.linphone.R.string.kt_contact_extension_label)))
            onView(withId(org.linphone.R.id.save_contact_button)).check(matches(not(isEnabled())))
        }
    }

    @Test
    fun validLocalContactSavesAndShowsCallView() {
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", false)
        ).use {
            onView(withId(org.linphone.R.id.setup_name_input)).perform(replaceText("Household contact"))
            onView(withId(org.linphone.R.id.setup_number_input)).perform(replaceText("512345"), closeSoftKeyboard())
            onView(withId(org.linphone.R.id.save_contact_button)).check(matches(isEnabled())).perform(click())
            onView(withId(org.linphone.R.id.call_view)).check(matches(isDisplayed()))
            onView(withId(org.linphone.R.id.call_button)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun authorisedCredentialAllowsLocalContactChange() {
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", true)
                .putExtra("credential_result", "authorized")
        ).use {
            onView(withId(org.linphone.R.id.change_contact_button)).perform(click())
            onView(withId(org.linphone.R.id.setup_view)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun noDeviceCredentialShowsSetupAfterNeutralNoticePath() {
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", true)
                .putExtra("credential_result", "no_device_credential")
        ).use {
            onView(withId(org.linphone.R.id.change_contact_button)).perform(click())
            onView(withId(org.linphone.R.id.setup_view)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun cancelledCredentialKeepsExistingCallView() {
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", true)
                .putExtra("credential_result", "cancelled")
        ).use {
            onView(withId(org.linphone.R.id.change_contact_button)).perform(click())
            onView(withId(org.linphone.R.id.call_view)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun oneTapCallUsesValidatedHouseholdExtension() {
        val scenario = ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", true)
        )
        scenario.use {
            onView(withId(org.linphone.R.id.call_button)).perform(click())
            scenario.onActivity { activity ->
                assertEquals("512345", activity.lastValidatedCallExtension)
            }
        }
    }

    @Test
    fun managedContactRemovalFallsBackToLocalSetup() {
        // I-05: The host models the post-MDM-removal state with neither a managed nor local contact.
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", false)
                .putExtra("managed", false)
        ).use {
            onView(withId(org.linphone.R.id.setup_view)).check(matches(isDisplayed()))
            onView(withId(org.linphone.R.id.call_view)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun managedContactHidesChangeControl() {
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", true)
                .putExtra("managed", true)
        ).use {
            onView(withId(org.linphone.R.id.call_view)).check(matches(isDisplayed()))
            onView(withId(org.linphone.R.id.change_contact_button)).check(matches(not(isDisplayed())))
            onView(withId(org.linphone.R.id.call_button)).check(matches(isDisplayed()))
        }
    }
}
