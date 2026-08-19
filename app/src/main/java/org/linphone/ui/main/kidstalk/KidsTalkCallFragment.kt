/*
 * Kids.Talk — Single Contact Call Screen
 *
 * Presents one effective household contact. Managed data is immutable locally;
 * a locally stored contact can be changed only through a one-time device gate.
 */
package org.linphone.ui.main.kidstalk

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.R
import org.linphone.core.RegistrationState

class KidsTalkCallFragment : Fragment() {
    private lateinit var callView: LinearLayout
    private lateinit var setupView: LinearLayout
    private lateinit var contactNameLabel: TextView
    private lateinit var callButton: Button
    private lateinit var changeContactButton: Button
    private lateinit var setupNameInput: EditText
    private lateinit var setupNumberInput: EditText
    private lateinit var saveContactButton: Button
    private lateinit var credentialGate: DeviceCredentialGate
    private lateinit var contactRepository: KidsTalkContactStore

    private var testDependencies: KidsTalkCallTestDependencies? = null
    private var effectiveContact: ResolvedKidsTalkContact? = null

    /** Installed only by an instrumentation FragmentFactory before lifecycle creation. */
    internal fun installTestDependencies(dependencies: KidsTalkCallTestDependencies) {
        testDependencies = dependencies
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_kidstalk_call, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callView = view.findViewById(R.id.call_view)
        setupView = view.findViewById(R.id.setup_view)
        contactNameLabel = view.findViewById(R.id.contact_name_label)
        callButton = view.findViewById(R.id.call_button)
        changeContactButton = view.findViewById(R.id.change_contact_button)
        setupNameInput = view.findViewById(R.id.setup_name_input)
        setupNumberInput = view.findViewById(R.id.setup_number_input)
        saveContactButton = view.findViewById(R.id.save_contact_button)

        val dependencies = testDependencies
        contactRepository = dependencies?.contactStore
            ?: KidsTalkContactRepository(requireContext().applicationContext)
        credentialGate = dependencies?.credentialGateFactory(::onContactChangeAuthorization)
            ?: AndroidDeviceCredentialGate(this, ::onContactChangeAuthorization)

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) = validateSetupForm()
        }
        setupNameInput.addTextChangedListener(watcher)
        setupNumberInput.addTextChangedListener(watcher)

        saveContactButton.setOnClickListener { saveContact() }
        callButton.setOnClickListener { placeCall() }
        changeContactButton.setOnClickListener { credentialGate.requestAuthorization() }

        if (testDependencies == null) {
            coreContext.mdmConfigAppliedEvent.observe(viewLifecycleOwner) { event ->
                event.consume { refresh() }
            }
            coreContext.mdmConfigRemovedEvent.observe(viewLifecycleOwner) { event ->
                event.consume { refresh() }
            }
        }
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        effectiveContact = contactRepository.resolve()
        effectiveContact?.let(::showCallView) ?: showSetupView()
    }

    private fun showCallView(resolved: ResolvedKidsTalkContact) {
        contactNameLabel.text = resolved.contact.name
        changeContactButton.visibility = if (resolved.isManaged) View.GONE else View.VISIBLE
        callView.visibility = View.VISIBLE
        setupView.visibility = View.GONE
    }

    private fun onContactChangeAuthorization(result: DeviceCredentialGateResult) {
        when (result) {
            DeviceCredentialGateResult.Authorized -> showSetupView()
            DeviceCredentialGateResult.NoDeviceCredential -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.kt_contact_change_unprotected_notice),
                    Toast.LENGTH_LONG
                ).show()
                showSetupView()
            }
            DeviceCredentialGateResult.Cancelled -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.kt_contact_change_cancelled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showSetupView() {
        val localContact = contactRepository.loadLocalContact()
        setupNameInput.setText(localContact?.name.orEmpty())
        setupNumberInput.setText(localContact?.extension.orEmpty())
        callView.visibility = View.GONE
        setupView.visibility = View.VISIBLE
        validateSetupForm()
    }

    private fun validateSetupForm() {
        val name = setupNameInput.text.toString().trim()
        val extension = setupNumberInput.text.toString().trim()
        saveContactButton.isEnabled = name.isNotEmpty() && KidsTalkContactPolicy.isValidExtension(extension)
    }

    private fun saveContact() {
        val contact = KidsTalkContact(
            name = setupNameInput.text.toString().trim(),
            extension = setupNumberInput.text.toString().trim()
        )
        if (!contactRepository.saveLocal(contact)) {
            Toast.makeText(requireContext(), getString(R.string.kt_contact_save_failed), Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(requireContext(), getString(R.string.kt_contact_saved), Toast.LENGTH_SHORT).show()
        refresh()
    }

    private fun placeCall() {
        val contact = effectiveContact?.contact ?: return
        val core = coreContext.core
        if (core.defaultAccount?.state != RegistrationState.Ok) {
            Toast.makeText(requireContext(), getString(R.string.kt_call_not_ready), Toast.LENGTH_SHORT).show()
            return
        }
        // Internal extensions are validated locally; do not apply an international prefix.
        val address = core.interpretUrl(KidsTalkSipEndpoint.addressForExtension(contact.extension), false)
        if (address == null) {
            Toast.makeText(requireContext(), getString(R.string.kt_call_failed), Toast.LENGTH_SHORT).show()
            return
        }
        coreContext.startCall(address)
    }
}
