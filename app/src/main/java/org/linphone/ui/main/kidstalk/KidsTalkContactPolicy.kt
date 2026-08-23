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

/** A validated household contact usable by the fixed internal SIP endpoint. */
data class KidsTalkContact(val name: String, val extension: String)

/** The effective contact and whether managed configuration makes it immutable locally. */
data class ResolvedKidsTalkContact(val contact: KidsTalkContact, val isManaged: Boolean)

/** Managed configuration is authoritative; local encrypted data is the fallback only. */
object KidsTalkContactResolver {
    fun resolve(
        managed: KidsTalkContact?,
        local: KidsTalkContact?
    ): ResolvedKidsTalkContact? = when {
        managed != null -> ResolvedKidsTalkContact(managed, isManaged = true)
        local != null -> ResolvedKidsTalkContact(local, isManaged = false)
        else -> null
    }
}

/**
 * Fixed SIP endpoint for this build. Transport configuration remains in the registration flow.
 */
object KidsTalkSipEndpoint {
    const val DOMAIN = "pbx.kids.talk"
    const val PORT = 5160

    fun addressForExtension(extension: String): String = "sip:$extension@$DOMAIN:$PORT"
}
