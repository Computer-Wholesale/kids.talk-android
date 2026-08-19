package org.linphone.ui.main.kidstalk

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.not
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
    fun managedContactHidesChangeControl() {
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", true)
                .putExtra("managed", true)
        ).use {
            onView(withId(org.linphone.R.id.call_view)).check(matches(isDisplayed()))
            onView(withId(org.linphone.R.id.change_contact_button)).check(matches(isNotDisplayed()))
            onView(withId(org.linphone.R.id.call_button)).check(matches(isDisplayed()))
        }
    }
}
