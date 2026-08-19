package org.linphone.ui.main.kidstalk

import android.content.Intent
import android.graphics.Rect
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KidsTalkScaleLayoutInstrumentedTest {
    @Test
    fun callControlsRemainVisibleAtTheEmulatorScaleConfiguredByCi() {
        ActivityScenario.launch<KidsTalkFragmentTestHostActivity>(
            Intent(ApplicationProvider.getApplicationContext(), KidsTalkFragmentTestHostActivity::class.java)
                .putExtra("has_contact", true)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.findViewById<android.view.View>(android.R.id.content)
                val rootBounds = Rect().also(root::getGlobalVisibleRect)
                listOf(org.linphone.R.id.contact_name_label, org.linphone.R.id.call_button).forEach { id ->
                    val bounds = Rect()
                    val visible = activity.findViewById<android.view.View>(id).getGlobalVisibleRect(bounds)
                    assertTrue("Control $id must remain visible", visible)
                    assertTrue("Control $id must fit horizontally", rootBounds.contains(bounds.left, bounds.top))
                    assertTrue("Control $id must fit vertically", rootBounds.contains(bounds.right - 1, bounds.bottom - 1))
                }
            }
        }
    }
}
