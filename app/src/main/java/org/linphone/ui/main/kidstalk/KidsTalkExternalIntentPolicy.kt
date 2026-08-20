package org.linphone.ui.main.kidstalk

/**
 * Decides whether an external intent is a separately approved Kids.Talk provisioning path.
 * All generic call and legacy Linphone URI inputs are contained by the normal home route.
 */
internal object KidsTalkExternalIntentPolicy {
    internal fun acceptsProvisioning(action: String?, scheme: String?): Boolean =
        action == "android.intent.action.VIEW" && scheme == "kidstalk-config"

    internal fun containsExternalInput(action: String?, scheme: String?): Boolean =
        action != null && action != "android.intent.action.MAIN" && !acceptsProvisioning(action, scheme)
}
