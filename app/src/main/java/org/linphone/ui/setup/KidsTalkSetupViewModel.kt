/*
 * Kids.Talk — Setup ViewModel
 *
 * Handles SIP account registration against pbx.kids.talk:5160.
 * Follows the contract established by ThirdPartySipAccountLoginViewModel:
 * LiveData fields, AuthInfo/Account via Factory/Core,
 * CoreListenerStub.onAccountRegistrationStateChanged with RegistrationState/Reason mapping,
 * rollback via removeAuthInfo/removeAccount.
 *
 * Privacy Invariant: No raw SDK/SIP error strings are ever exposed to the user.
 * All error messages are mapped to approved user-facing copy via string resources.
 */
package org.linphone.ui.setup

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.R
import org.linphone.ui.main.kidstalk.KidsTalkSipEndpoint
import org.linphone.core.Account
import org.linphone.core.AuthInfo
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.Reason
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import org.linphone.core.tools.Log
import org.linphone.utils.AppUtils

/**
 * ViewModel for KidsTalkSetupActivity.
 *
 * Exposes:
 * - [isLoading]: true while registration is in progress
 * - [errorMessage]: user-facing error copy (never raw SDK/SIP detail), null when no error
 * - [registrationSuccess]: true when the account registers successfully
 *
 * The Activity calls [registerAccount] with the validated username and password.
 */
class KidsTalkSetupViewModel : ViewModel() {

    companion object {
        private const val TAG = "[KidsTalk Setup ViewModel]"
        private val PBX_TRANSPORT = TransportType.Udp
    }

    val isLoading = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)
    val registrationSuccess = MutableLiveData(false)

    /** Bound by the Activity's TextWatcher — stores the current username input. */
    var username: String = ""

    /** Bound by the Activity's TextWatcher — stores the current password input. */
    var password: String = ""

    private lateinit var newlyCreatedAuthInfo: AuthInfo
    private lateinit var newlyCreatedAccount: Account

    private val coreListener = object : CoreListenerStub() {
        @WorkerThread
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            if (::newlyCreatedAccount.isInitialized && account == newlyCreatedAccount) {
                Log.i("$TAG Account registration state: [$state] ($message)")
                when (state) {
                    RegistrationState.Ok -> {
                        isLoading.postValue(false)
                        core.removeListener(this)
                        core.defaultAccount = newlyCreatedAccount
                        registrationSuccess.postValue(true)
                    }
                    RegistrationState.Failed -> {
                        isLoading.postValue(false)
                        core.removeListener(this)
                        // Privacy Invariant: map Reason to approved user-facing copy.
                        // Never expose raw SIP error text or Reason enum names.
                        val userFacingError = when (account.error) {
                            Reason.Forbidden -> AppUtils.getString(
                                R.string.setup_error_invalid_credentials
                            )
                            Reason.NotFound -> AppUtils.getString(
                                R.string.setup_error_account_not_found
                            )
                            Reason.IOError -> AppUtils.getString(
                                R.string.setup_error_network
                            )
                            else -> AppUtils.getString(
                                R.string.setup_error_generic
                            )
                        }
                        errorMessage.postValue(userFacingError)
                        // Rollback: remove the failed auth/account from Core
                        Log.e("$TAG Registration failed, rolling back account")
                        core.removeAuthInfo(newlyCreatedAuthInfo)
                        core.removeAccount(newlyCreatedAccount)
                    }
                    else -> { /* Progress states — no action */ }
                }
            }
        }
    }

    /**
     * Clears the current error message. Called by the Activity after displaying the error.
     */
    fun clearError() {
        errorMessage.value = null
    }

    /**
     * Initiates SIP account registration against the Kids.Talk PBX.
     *
     * @param password The account password
     */
    @UiThread
    fun registerAccount(username: String, password: String) {
        isLoading.value = true
        errorMessage.value = null
        registrationSuccess.value = false

        coreContext.postOnCoreThread { core ->
            val authInfo = Factory.instance().createAuthInfo(
                username,
                null, // userid
                password,
                null, // ha1
                null, // realm
                KidsTalkSipEndpoint.DOMAIN
            )

            val accountParams = core.createAccountParams()
            val identity = Factory.instance().createAddress("sip:$username@$KidsTalkSipEndpoint.DOMAIN")
            identity?.port = KidsTalkSipEndpoint.PORT
            accountParams.identityAddress = identity

            val serverAddress = Factory.instance().createAddress("sip:$KidsTalkSipEndpoint.DOMAIN")
            serverAddress?.port = KidsTalkSipEndpoint.PORT
            serverAddress?.transport = PBX_TRANSPORT
            accountParams.serverAddress = serverAddress

            accountParams.isRegisterEnabled = true

            val account = core.createAccount(accountParams)

            newlyCreatedAuthInfo = authInfo
            newlyCreatedAccount = account

            core.addListener(coreListener)
            core.addAuthInfo(authInfo)
            core.addAccount(account)

            Log.i("$TAG Registration initiated for $username@$KidsTalkSipEndpoint.DOMAIN:$KidsTalkSipEndpoint.PORT")
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Safety: remove listener if ViewModel is destroyed mid-registration
        coreContext.postOnCoreThread { core ->
            core.removeListener(coreListener)
        }
    }
}
