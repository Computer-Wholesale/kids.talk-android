/*
 * Kids.Talk — First-run setup screen
 *          then registers the SIP account against pbx.kids.talk:5160.
 * Step 2: Explains the Microphone permission and requests it.
 * Step 3: Explains the Notifications permission and requests it (Android 13+).
 * Step 4: Explains the Full-screen-intent permission and opens settings (Android 14+).
 */
package org.linphone.ui.setup

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.linphone.R
import org.linphone.ui.main.kidstalk.KidsTalkContactPolicy
import org.linphone.ui.main.kidstalk.KidsTalkSetupInputPolicy
import org.linphone.ui.main.kidstalk.SetupUsernameValidation
import org.linphone.ui.main.MainActivity

@UiThread
class KidsTalkSetupActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "[KidsTalk Setup]"
    }

    private lateinit var viewModel: KidsTalkSetupViewModel

    // ── Step 1: Login card ───────────────────────────────────────────────────
    private lateinit var loginCard: View
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordInput: TextInputEditText
    private lateinit var connectButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView

    // ── Step 2: Microphone permission card ───────────────────────────────────
    private lateinit var permMicCard: View
    private lateinit var allowMicButton: Button
    private lateinit var skipMicButton: Button

    // ── Step 3: Notifications permission card ────────────────────────────────
    private lateinit var permNotifCard: View
    private lateinit var allowNotifButton: Button
    private lateinit var skipNotifButton: Button

    // ── Step 4: Full-screen-intent permission card ───────────────────────────
    private lateinit var permFullscreenCard: View
    private lateinit var allowPermissionButton: Button
    private lateinit var skipPermissionButton: Button

    // ── Permission launchers ─────────────────────────────────────────────────

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Whether granted or denied, move to next step
        showNotifPermCard()
    }

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Whether granted or denied, move to next step
        showFullscreenPermCard()
    }

    private val fullScreenIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        launchMainActivity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kidstalk_setup)

        // Disable back navigation — user must complete setup
        onBackPressedDispatcher.addCallback { }

        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(
                insets.left,
                insets.top,
                insets.right,
                maxOf(insets.bottom, keyboard.bottom)
            )
            WindowInsetsCompat.CONSUMED
        }

        viewModel = ViewModelProvider(this)[KidsTalkSetupViewModel::class.java]

        // ── Bind Step 1 views ────────────────────────────────────────────────
        loginCard        = findViewById(R.id.login_card)
        usernameLayout   = findViewById(R.id.username_layout)
        usernameInput    = findViewById(R.id.username_input)
        passwordLayout   = findViewById(R.id.password_layout)
        passwordInput    = findViewById(R.id.password_input)
        connectButton    = findViewById(R.id.connect_button)
        progressBar      = findViewById(R.id.progress_bar)
        errorText        = findViewById(R.id.error_text)

        // ── Bind Step 2 views ────────────────────────────────────────────────
        permMicCard      = findViewById(R.id.perm_mic_card)
        allowMicButton   = findViewById(R.id.allow_mic_button)
        skipMicButton    = findViewById(R.id.skip_mic_button)

        // ── Bind Step 3 views ────────────────────────────────────────────────
        permNotifCard    = findViewById(R.id.perm_notif_card)
        allowNotifButton = findViewById(R.id.allow_notif_button)
        skipNotifButton  = findViewById(R.id.skip_notif_button)

        // ── Bind Step 4 views ────────────────────────────────────────────────
        permFullscreenCard    = findViewById(R.id.perm_fullscreen_card)
        allowPermissionButton = findViewById(R.id.allow_permission_button)
        skipPermissionButton  = findViewById(R.id.skip_permission_button)

        // Start with login card visible, all permission cards hidden
        loginCard.visibility          = View.VISIBLE
        permMicCard.visibility        = View.GONE
        permNotifCard.visibility      = View.GONE
        permFullscreenCard.visibility = View.GONE

        // ── Live validation ──────────────────────────────────────────────────
        usernameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val raw = s?.toString().orEmpty()
                viewModel.username = raw
                usernameLayout.error = when (KidsTalkSetupInputPolicy.validateUsername(raw)) {
                    SetupUsernameValidation.Empty,
                    SetupUsernameValidation.Valid -> null
                    SetupUsernameValidation.NonDigit -> getString(R.string.setup_username_digits_error)
                    SetupUsernameValidation.InvalidExtension -> getString(R.string.setup_username_length_error)
                }
                updateConnectButton()
            }
        })

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.password = s?.toString().orEmpty()
                updateConnectButton()
            }
        })

        passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && connectButton.isEnabled) {
                hideKeyboard()
                onConnectClicked()
                true
            } else {
                false
            }
        }

        connectButton.setOnClickListener {
            hideKeyboard()
            onConnectClicked()
        }

        // ── Step 2 buttons ───────────────────────────────────────────────────
        allowMicButton.setOnClickListener {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        skipMicButton.setOnClickListener { showNotifPermCard() }

        // ── Step 3 buttons ───────────────────────────────────────────────────
        allowNotifButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                showFullscreenPermCard()
            }
        }
        skipNotifButton.setOnClickListener { showFullscreenPermCard() }

        // ── Step 4 buttons ───────────────────────────────────────────────────
        allowPermissionButton.setOnClickListener {
            requestFullScreenIntentPermission()
        }
        skipPermissionButton.setOnClickListener { launchMainActivity() }

        // ── Observe ViewModel state ──────────────────────────────────────────
        viewModel.isLoading.observe(this) { loading ->
            connectButton.isEnabled = !loading && isInputValid(
                usernameInput.text?.toString().orEmpty().trim(),
                passwordInput.text?.toString().orEmpty()
            )
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            usernameInput.isEnabled = !loading
            passwordInput.isEnabled = !loading
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (msg != null) {
                errorText.text = msg
                errorText.visibility = View.VISIBLE
                viewModel.clearError()
            } else {
                errorText.visibility = View.GONE
            }
        }

        viewModel.registrationSuccess.observe(this) { success ->
            if (success) {
                // After login succeeds, start the permission explanation flow
                showMicPermCard()
            }
        }

        updateConnectButton()
    }

    // ── Permission card transitions ──────────────────────────────────────────

    private fun showMicPermCard() {
        loginCard.visibility          = View.GONE
        permMicCard.visibility        = View.VISIBLE
        permNotifCard.visibility      = View.GONE
        permFullscreenCard.visibility = View.GONE
    }

    private fun showNotifPermCard() {
        loginCard.visibility          = View.GONE
        permMicCard.visibility        = View.GONE
        // Only show notifications card on Android 13+; older versions auto-grant
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permNotifCard.visibility      = View.VISIBLE
            permFullscreenCard.visibility = View.GONE
        } else {
            showFullscreenPermCard()
        }
    }

    private fun showFullscreenPermCard() {
        loginCard.visibility          = View.GONE
        permMicCard.visibility        = View.GONE
        permNotifCard.visibility      = View.GONE
        // Only show full-screen card on Android 14+; older versions auto-grant
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permFullscreenCard.visibility = View.VISIBLE
        } else {
            launchMainActivity()
        }
    }

    private fun requestFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:$packageName")
            }
            fullScreenIntentLauncher.launch(intent)
        } else {
            launchMainActivity()
        }
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // ── Step 1: Login helpers ────────────────────────────────────────────────

    private fun onConnectClicked() {
        val username = usernameInput.text?.toString().orEmpty().trim()
        val password = passwordInput.text?.toString().orEmpty()
        if (!isInputValid(username, password)) return
        viewModel.registerAccount(username, password)
    }

    private fun updateConnectButton() {
        val username = usernameInput.text?.toString().orEmpty().trim()
        val password = passwordInput.text?.toString().orEmpty()
        connectButton.isEnabled = isInputValid(username, password)
    }

    private fun isInputValid(username: String, password: String): Boolean {
        if (!KidsTalkContactPolicy.isValidExtension(username)) return false
        return password.isNotEmpty()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }
}
