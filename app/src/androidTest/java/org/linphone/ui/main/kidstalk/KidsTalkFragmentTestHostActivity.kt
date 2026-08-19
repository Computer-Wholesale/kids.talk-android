package org.linphone.ui.main.kidstalk

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class KidsTalkFragmentTestHostActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val managed = intent.getBooleanExtra("managed", false)
            val contact = if (intent.getBooleanExtra("has_contact", false)) {
                KidsTalkContact("Household contact", "512345")
            } else {
                null
            }
            val store = object : KidsTalkContactStore {
                private var local = if (managed) null else contact
                override fun resolve(): ResolvedKidsTalkContact? = contact?.let {
                    ResolvedKidsTalkContact(it, isManaged = managed)
                } ?: local?.let { ResolvedKidsTalkContact(it, isManaged = false) }
                override fun loadLocalContact(): KidsTalkContact? = local
                override fun saveLocal(contact: KidsTalkContact): Boolean {
                    local = contact
                    return true
                }
            }
            val fragment = KidsTalkCallFragment().apply {
                installTestDependencies(
                    KidsTalkCallTestDependencies(store) { callback ->
                        object : DeviceCredentialGate {
                            override fun requestAuthorization() = callback(DeviceCredentialGateResult.Authorized)
                        }
                    }
                )
            }
            supportFragmentManager.beginTransaction().replace(android.R.id.content, fragment).commitNow()
        }
    }
}
