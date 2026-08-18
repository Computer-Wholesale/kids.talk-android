package org.linphone.ui.main.kidstalk

/**
 * PBX-backed policy for household contact extensions.
 *
 * The Kids.Talk PBX accepts internal source and destination extensions only when they
 * contain exactly six decimal digits. International-prefix expansion is intentionally
 * not applied to these internal extensions.
 */
object KidsTalkContactPolicy {
    private val extensionPattern = Regex("^[0-9]{6}$")

    fun isValidExtension(value: String): Boolean = extensionPattern.matches(value)
}

/**
 * Fixed SIP endpoint for this build. Transport configuration remains in the registration flow.
 */
object KidsTalkSipEndpoint {
    const val DOMAIN = "pbx.kids.talk"
    const val PORT = 5160

    fun addressForExtension(extension: String): String = "sip:$extension@$DOMAIN:$PORT"
}
