/*
 * Kids.Talk — Single Contact Call Screen
 *
 * Shows a big "Call" button for the one pre-configured contact.
 * If no contact has been saved yet, shows a setup form instead.
 *
 * KID-263: Info icon (top-right) opens KidsTalkAboutBottomSheet.
 */
package org.linphone.ui.main.kidstalk

import android.content.Context
import android.os.Bundle
import org.linphone.LinphoneApplication.Companion.coreContext
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.linphone.R

class KidsTalkCallFragment : Fragment() {

    companion object {
        private const val PREFS_NAME = "kidstalk_contact"
        private const val KEY_NAME = "contact_name"
        private const val KEY_NUMBER = "contact_number"
    }

    // ── Views ────────────────────────────────────────────────────────────────
    private lateinit var callView: LinearLayout
    private lateinit var setupView: LinearLayout
    private lateinit var contactNameLabel: TextView
    private lateinit var callButton: Button
    private lateinit var changeContactButton: Button
    private lateinit var setupNameInput: EditText
    private lateinit var setupNumberInput: EditText
    private lateinit var saveContactButton: Button
    private lateinit var infoButton: ImageButton

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
        infoButton = view.findViewById(R.id.info_button)

        // Live validation on setup form
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) { validateSetupForm() }
        }
        setupNameInput.addTextChangedListener(watcher)
        setupNumberInput.addTextChangedListener(watcher)

        saveContactButton.setOnClickListener { saveContact() }
        callButton.setOnClickListener { placeCall() }
        changeContactButton.setOnClickListener { showSetupView() }

        // KID-263: info icon opens the About / Support bottom sheet (KID-270)
        infoButton.setOnClickListener {
            KidsTalkAboutBottomSheet()
                .show(parentFragmentManager, KidsTalkAboutBottomSheet.TAG)
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    // ── State ────────────────────────────────────────────────────────────────

    private fun refresh() {
        val (name, number) = loadContact()
        if (name != null && number != null) {
            showCallView(name, number)
        } else {
            showSetupView()
        }
    }

    private fun showCallView(name: String, number: String) {
        contactNameLabel.text = name
        callButton.tag = number          // store number for placeCall()
        callView.visibility = View.VISIBLE
        setupView.visibility = View.GONE
    }

    private fun showSetupView() {
        val (name, number) = loadContact()
        setupNameInput.setText(name ?: "")
        setupNumberInput.setText(number ?: "")
        callView.visibility = View.GONE
        setupView.visibility = View.VISIBLE
        validateSetupForm()
    }

    // ── Validation ───────────────────────────────────────────────────────────

    private fun validateSetupForm() {
        val name = setupNameInput.text.toString().trim()
        val number = setupNumberInput.text.toString().trim()
        saveContactButton.isEnabled = name.isNotEmpty() && isValidNumber(number)
    }

    private fun isValidNumber(number: String): Boolean {
        // Accept any 4-10 digit number — no prefix restriction on the contact
        if (number.length < 4 || number.length > 10) return false
        return number.all { it.isDigit() }
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    private fun saveContact() {
        val name = setupNameInput.text.toString().trim()
        val number = setupNumberInput.text.toString().trim()
        if (name.isEmpty() || !isValidNumber(number)) return

        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_NAME, name)
            .putString(KEY_NUMBER, number)
            .apply()

        Toast.makeText(requireContext(), getString(R.string.kt_contact_saved), Toast.LENGTH_SHORT).show()
        showCallView(name, number)
    }

    private fun loadContact(): Pair<String?, String?> {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_NAME, null)
        val number = prefs.getString(KEY_NUMBER, null)
        return Pair(name, number)
    }

    // ── Call ─────────────────────────────────────────────────────────────────

    private fun placeCall() {
        val number = callButton.tag as? String ?: return
        // coreContext is imported via LinphoneApplication.Companion.coreContext
        val core = coreContext.core

        // Build the SIP address: sip:NUMBER@pbx.kids.talk:5160
        val sipAddress = "sip:$number@pbx.kids.talk:5160"
        val address = core.interpretUrl(sipAddress, false)
        if (address == null) {
            Toast.makeText(requireContext(), getString(R.string.kt_call_failed), Toast.LENGTH_SHORT).show()
            return
        }
        coreContext.startCall(address)
    }
}
